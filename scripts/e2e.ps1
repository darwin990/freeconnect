# Flujo E2E de humo para FreeConnect (perfil dev, H2 en memoria).
# Uso: powershell -ExecutionPolicy Bypass -File scripts/e2e.ps1
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080/api/v1"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token,
        [object]$Body
    )
    $headers = @{}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $params = @{
        Method      = $Method
        Uri         = "$base$Path"
        Headers     = $headers
        ContentType = "application/json"
    }
    if ($null -ne $Body) {
        $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
    }
    return Invoke-RestMethod @params
}

$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$clientEmail = "cliente$suffix@freeconnect.test"
$freelancerEmail = "freelancer$suffix@freeconnect.test"
$password = "FreeConnect#2026Aa"

Write-Host "== RF-01: registro y login =="
$client = Invoke-Api POST "/auth/register" $null @{
    email = $clientEmail; password = $password; displayName = "Cliente E2E"; role = "CLIENT"
}
$freelancer = Invoke-Api POST "/auth/register" $null @{
    email = $freelancerEmail; password = $password; displayName = "Freelancer E2E"; role = "FREELANCER"
}
$clientToken = $client.accessToken
$freelancerToken = $freelancer.accessToken
$clientId = $client.user.id
$freelancerId = $freelancer.user.id
Write-Host "Cliente: $clientId / Freelancer: $freelancerId"

$me = Invoke-Api GET "/auth/me" $clientToken $null
if ($me.email -ne $clientEmail) { throw "RF-01 fallo: /auth/me no coincide" }

Write-Host "== RF-02: perfiles =="
Invoke-Api PUT "/profiles/me" $freelancerToken @{
    headline = "Backend Java"; bio = "Especialista en Spring Boot"; hourlyRate = 45;
    currency = "USD"; availability = "AVAILABLE"; skills = @("Java", "Spring Boot")
} | Out-Null
$profile = Invoke-Api GET "/profiles/me" $freelancerToken $null
if (-not $profile.id) { throw "RF-02 fallo: perfil no creado" }

Write-Host "== RF-03: proyecto publicado y busqueda =="
$project = Invoke-Api POST "/projects" $clientToken @{
    title = "API de reservas"; description = "Construir una API REST con Spring Boot y PostgreSQL";
    category = "SOFTWARE"; budget = 1000; currency = "USD"; modality = "REMOTE";
    deadline = (Get-Date).AddDays(30).ToString("yyyy-MM-dd"); skills = @("Java", "Spring Boot")
}
$projectId = $project.id
Invoke-Api PATCH "/projects/$projectId/status" $clientToken @{ status = "PUBLISHED" } | Out-Null
$found = Invoke-Api GET "/projects?q=reservas" $null $null
if ($found.totalElements -lt 1) { throw "RF-03 fallo: busqueda sin resultados" }

Write-Host "== RF-04: propuesta =="
$proposal = Invoke-Api POST "/projects/$projectId/proposals" $freelancerToken @{
    coverLetter = "Tengo 8 anos de experiencia construyendo APIs REST con Spring Boot y PostgreSQL.";
    amount = 1000; currency = "USD"; estimatedDays = 20
}
$proposalId = $proposal.id
$proposals = Invoke-Api GET "/projects/$projectId/proposals" $clientToken $null
if ($proposals.Count -lt 1) { throw "RF-04 fallo: propuesta no listada" }

Write-Host "== RF-05: aceptar propuesta y crear contrato con 2 hitos =="
$contract = Invoke-Api POST "/proposals/$proposalId/accept" $clientToken @{
    commissionPlan = "PREMIUM"; milestoneType = "EQUAL_SPLIT"; parts = 2
}
$contractId = $contract.id
if ($contract.status -ne "ACTIVE") { throw "RF-05 fallo: contrato no activo" }
if ($contract.milestones.Count -ne 2) { throw "RF-05 fallo: hitos esperados 2" }

Write-Host "== RF-06: entregables, tareas, comentarios y aprobacion =="
$milestone1 = $contract.milestones[0]
$withDeliverable = Invoke-Api POST "/contracts/$contractId/milestones/$($milestone1.id)/deliverables" $freelancerToken @{
    title = "Modulo de autenticacion"; description = "JWT + refresh tokens";
    url = "https://example.com/repo/auth"
}
$deliverable = $withDeliverable.milestones[0].deliverables[0]

$withTask = Invoke-Api POST "/contracts/$contractId/milestones/$($milestone1.id)/deliverables/$($deliverable.id)/tasks" $freelancerToken @{
    title = "Implementar login"
}
$task = $withTask.milestones[0].deliverables[0].tasks[0]
Invoke-Api PATCH "/contracts/$contractId/milestones/$($milestone1.id)/deliverables/$($deliverable.id)/tasks/$($task.id)" $freelancerToken @{ done = $true } | Out-Null
Invoke-Api POST "/contracts/$contractId/milestones/$($milestone1.id)/deliverables/$($deliverable.id)/comments" $clientToken @{
    body = "Revisar manejo de expiracion del token"
} | Out-Null

$submitted = Invoke-Api POST "/contracts/$contractId/milestones/$($milestone1.id)/submit" $freelancerToken $null
if ($submitted.milestones[0].status -ne "SUBMITTED") { throw "RF-06 fallo: hito no enviado" }
$approved = Invoke-Api POST "/contracts/$contractId/milestones/$($milestone1.id)/approve" $clientToken $null
if ($approved.milestones[0].status -ne "APPROVED") { throw "RF-06 fallo: hito no aprobado" }

$milestone2 = $approved.milestones[1]
$step2 = Invoke-Api POST "/contracts/$contractId/milestones/$($milestone2.id)/deliverables" $freelancerToken @{
    title = "Modulo de reservas"; description = "CRUD y disponibilidad"; url = "https://example.com/repo/bookings"
}
$deliverable2 = $step2.milestones[1].deliverables[0]
Invoke-Api POST "/contracts/$contractId/milestones/$($milestone2.id)/submit" $freelancerToken $null | Out-Null
$completed = Invoke-Api POST "/contracts/$contractId/milestones/$($milestone2.id)/approve" $clientToken $null
if ($completed.status -ne "COMPLETED") { throw "RF-06 fallo: contrato no completado" }
if ($completed.progress -ne 100) { throw "RF-06 fallo: progreso esperado 100" }

$mine = Invoke-Api GET "/contracts" $clientToken $null
if ($mine.Count -lt 1) { throw "Contratos no listados" }

Write-Host "== RF-04: evaluar, rechazar y retirar propuestas =="
$proposalProject = Invoke-Api POST "/projects" $clientToken @{
    title = "Proyecto de propuestas"; description = "Proyecto temporal para validar el ciclo de propuestas";
    category = "SOFTWARE"; budget = 600; currency = "USD"; modality = "REMOTE";
    deadline = (Get-Date).AddDays(15).ToString("yyyy-MM-dd"); skills = @("Java")
}
Invoke-Api PATCH "/projects/$($proposalProject.id)/status" $clientToken @{ status = "PUBLISHED" } | Out-Null
$proposalA = Invoke-Api POST "/projects/$($proposalProject.id)/proposals" $freelancerToken @{
    coverLetter = "Propuesta temporal para validar evaluacion, rechazo y retiro dentro del flujo.";
    amount = 600; currency = "USD"; estimatedDays = 12
}
$evaluated = Invoke-Api POST "/proposals/$($proposalA.id)/evaluate" $clientToken @{
    score = 4; note = "Buen enfoque tecnico"
}
if ($evaluated.evaluationScore -ne 4) { throw "RF-04 fallo: evaluacion no registrada" }
$rejected = Invoke-Api POST "/proposals/$($proposalA.id)/reject" $clientToken $null
if ($rejected.status -ne "REJECTED") { throw "RF-04 fallo: propuesta no rechazada" }

$proposalB = Invoke-Api POST "/projects/$($proposalProject.id)/proposals" $freelancerToken @{
    coverLetter = "Segunda propuesta temporal que el freelancer retirara antes del cierre.";
    amount = 550; currency = "USD"; estimatedDays = 10
}
$withdrawn = Invoke-Api DELETE "/proposals/$($proposalB.id)" $freelancerToken $null
if ($withdrawn.status -ne "WITHDRAWN") { throw "RF-04 fallo: propuesta no retirada" }
$proposalList = Invoke-Api GET "/projects/$($proposalProject.id)/proposals" $clientToken $null
if ($proposalList.Count -lt 2) { throw "RF-04 fallo: propuestas no listadas" }

Write-Host "== Mensajeria: abrir conversacion, enviar, leer y archivar =="
$conversation = Invoke-Api POST "/contracts/$contractId/conversations" $clientToken $null
$conversationId = $conversation.id
if (-not $conversationId) { throw "Mensajeria fallo: conversacion no creada" }
$afterSend = Invoke-Api POST "/conversations/$conversationId/messages" $clientToken @{
    body = "Buen trabajo, revisemos el cierre del contrato."
}
if ($afterSend.messages.Count -lt 1) { throw "Mensajeria fallo: mensaje no registrado" }
Invoke-Api POST "/conversations/$conversationId/messages" $freelancerToken @{
    body = "Gracias, quedo atento a cualquier ajuste."
} | Out-Null
$unread = Invoke-Api GET "/conversations/$conversationId" $freelancerToken $null
if ($unread.unread -lt 1) { throw "Mensajeria fallo: no hay mensajes no leidos" }
$read = Invoke-Api POST "/conversations/$conversationId/read" $freelancerToken $null
if ($read.unread -ne 0) { throw "Mensajeria fallo: no se marcaron como leidos" }
$archivedConversation = Invoke-Api POST "/conversations/$conversationId/archive" $clientToken $null
if ($archivedConversation.status -ne "ARCHIVED") { throw "Mensajeria fallo: conversacion no archivada" }
$myConversations = Invoke-Api GET "/conversations" $freelancerToken $null
if ($myConversations.Count -lt 1) { throw "Mensajeria fallo: conversaciones no listadas" }

Write-Host "== Resenas: cliente califica al freelancer y actualiza reputacion =="
$review = Invoke-Api POST "/contracts/$contractId/reviews" $clientToken @{
    rating = 5; comment = "Entrega puntual y codigo de calidad."
}
if ($review.targetId -ne $freelancerId) { throw "Resenas fallo: destinatario incorrecto" }
$targetReviews = Invoke-Api GET "/users/$freelancerId/reviews" $freelancerToken $null
if ($targetReviews.Count -lt 1) { throw "Resenas fallo: resena no listada" }
$contractReviews = Invoke-Api GET "/contracts/$contractId/reviews" $clientToken $null
if ($contractReviews.Count -lt 1) { throw "Resenas fallo: resena no asociada al contrato" }
$ratedProfile = Invoke-Api GET "/profiles/$($profile.id)" $freelancerToken $null
if ($ratedProfile.ratingCount -lt 1 -or [double]$ratedProfile.ratingAverage -ne 5) {
    throw "Resenas fallo: reputacion no actualizada"
}

Write-Host "== Pagos: liberar pago con comision y consultar historial =="
$payment = Invoke-Api POST "/contracts/$contractId/payments" $clientToken @{ amount = 1000 }
if ($payment.status -ne "RELEASED") { throw "Pagos fallo: pago no liberado" }
if ([double]$payment.commission.amount -ne 50) { throw "Pagos fallo: comision PREMIUM esperada 50" }
if ([double]$payment.netAmount.amount -ne 950) { throw "Pagos fallo: neto esperado 950" }
$contractPayments = Invoke-Api GET "/contracts/$contractId/payments" $freelancerToken $null
if ($contractPayments.Count -lt 1) { throw "Pagos fallo: historial vacio" }
$myPayments = Invoke-Api GET "/payments" $freelancerToken $null
if ($myPayments.Count -lt 1) { throw "Pagos fallo: pagos del beneficiario no listados" }

Write-Host "== Notificaciones: outbox in-app generado por eventos =="
$clientNotifications = Invoke-Api GET "/notifications" $clientToken $null
if ($clientNotifications.Count -lt 1) { throw "Notificaciones fallo: cliente sin notificaciones" }
$freelancerNotifications = Invoke-Api GET "/notifications" $freelancerToken $null
if ($freelancerNotifications.Count -lt 1) { throw "Notificaciones fallo: freelancer sin notificaciones" }
if (-not ($freelancerNotifications | Where-Object { $_.type -eq "PAYMENT_RELEASED" })) {
    throw "Notificaciones fallo: falta notificacion de pago"
}
if (-not ($freelancerNotifications | Where-Object { $_.type -eq "REVIEW_RECEIVED" })) {
    throw "Notificaciones fallo: falta notificacion de resena"
}

Write-Host "== Admin: login, reportes Visitor y auditoria =="
$admin = Invoke-Api POST "/auth/login" $null @{
    email = "admin@freeconnect.io"; password = "Admin#FreeConnect2026"
}
$adminToken = $admin.accessToken
$reports = Invoke-Api GET "/admin/reports" $adminToken $null
if ($reports.Count -ne 3) { throw "Admin fallo: se esperaban 3 reportes" }
$audit = Invoke-Api GET "/admin/audit" $adminToken $null
if ($audit.Count -lt 1) { throw "Admin fallo: auditoria vacia" }

Write-Host "== Admin: moderacion de proyecto y suspension de usuario =="
$moderationProject = Invoke-Api POST "/projects" $clientToken @{
    title = "Proyecto para moderar"; description = "Proyecto temporal para validar la moderacion administrativa";
    category = "SOFTWARE"; budget = 200; currency = "USD"; modality = "REMOTE";
    deadline = (Get-Date).AddDays(10).ToString("yyyy-MM-dd"); skills = @("Java")
}
Invoke-Api PATCH "/projects/$($moderationProject.id)/status" $clientToken @{ status = "PUBLISHED" } | Out-Null
$paused = Invoke-Api POST "/admin/projects/$($moderationProject.id)/moderate" $adminToken @{ status = "PAUSED" }
if ($paused.status -ne "PAUSED") { throw "Admin fallo: proyecto no pausado" }
$cancelled = Invoke-Api POST "/admin/projects/$($moderationProject.id)/moderate" $adminToken @{ status = "CANCELLED" }
if ($cancelled.status -ne "CANCELLED") { throw "Admin fallo: proyecto no cancelado" }
$suspended = Invoke-Api POST "/admin/users/$freelancerId/suspend" $adminToken $null
if ($suspended.status -ne "SUSPENDED") { throw "Admin fallo: usuario no suspendido" }

Write-Host ""
Write-Host "E2E OK -> contrato $contractId estado $($completed.status) progreso $($completed.progress)%"
Write-Host "Modulos OK -> mensajeria, resenas, pagos, notificaciones, reportes, moderacion y auditoria"
