import api from '@/services/api';
import type { ApiResponse, PageResponse } from '@/types/api.types';
import type { NotificationItem } from '@/types/notification.types';

const NOTIFICATION_URL = '/api/notifications';

export const notificationService = {
  getMyNotifications: async (page = 0, size = 10): Promise<PageResponse<NotificationItem>> => {
    const res = await api.get<ApiResponse<PageResponse<NotificationItem>>>(NOTIFICATION_URL, {
      params: { page, size },
    });
    return res.data.data;
  },

  countUnread: async (): Promise<number> => {
    const res = await api.get<ApiResponse<number>>(`${NOTIFICATION_URL}/unread-count`);
    return res.data.data;
  },

  markAsRead: async (id: number): Promise<void> => {
    await api.patch<ApiResponse<void>>(`${NOTIFICATION_URL}/${id}/read`);
  },

  markAllAsRead: async (): Promise<void> => {
    await api.patch<ApiResponse<void>>(`${NOTIFICATION_URL}/mark-all-read`);
  },
};
