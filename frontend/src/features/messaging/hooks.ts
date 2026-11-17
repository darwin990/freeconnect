import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/shared/api/queryKeys";
import type { Conversation } from "@/types/models";
import { messagingApi } from "./api";

const pollMs = Number(import.meta.env.VITE_MESSAGING_POLL_MS ?? 5000);

export function useConversations(enabled = true) {
  return useQuery({
    queryKey: queryKeys.conversations,
    queryFn: messagingApi.list,
    enabled,
    refetchInterval: pollMs > 0 ? pollMs : false,
  });
}

export function useConversation(id: string | undefined) {
  return useQuery({
    queryKey: queryKeys.conversation(id ?? ""),
    queryFn: () => messagingApi.get(id as string),
    enabled: Boolean(id),
    refetchInterval: pollMs > 0 ? pollMs : false,
  });
}

export function useSendMessage(conversationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: string) => messagingApi.send(conversationId, body),
    onSuccess: (conversation) => {
      queryClient.setQueryData(queryKeys.conversation(conversationId), conversation);
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations });
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });
    },
  });
}

export function useMarkConversationRead(conversationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => messagingApi.markRead(conversationId),
    onSuccess: (conversation: Conversation) => {
      queryClient.setQueryData(queryKeys.conversation(conversationId), conversation);
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations });
    },
  });
}

export function useArchiveConversation(conversationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => messagingApi.archive(conversationId),
    onSuccess: (conversation) => {
      queryClient.setQueryData(queryKeys.conversation(conversationId), conversation);
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations });
    },
  });
}
