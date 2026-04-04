import api from '@/services/api';
import type { ApiResponse } from '@/types/api.types';
import type {
  CandidateSuggestion,
  JobSuggestion,
  MatchScoreRequest,
  MatchScoreResult,
} from '@/types/matching.types';

const MATCHING_URL = '/api/matching';

export const matchingService = {
  suggestCandidatesForJob: async (jobId: number, limit = 10): Promise<CandidateSuggestion[]> => {
    const res = await api.get<ApiResponse<CandidateSuggestion[]>>(`${MATCHING_URL}/candidates/${jobId}`, {
      params: { limit },
    });
    return res.data.data;
  },

  suggestJobsForCandidate: async (candidateId: number, limit = 10): Promise<JobSuggestion[]> => {
    const res = await api.get<ApiResponse<JobSuggestion[]>>(`${MATCHING_URL}/jobs/${candidateId}`, {
      params: { limit },
    });
    return res.data.data;
  },

  calculateMatchScore: async (payload: MatchScoreRequest): Promise<MatchScoreResult> => {
    const res = await api.post<ApiResponse<MatchScoreResult>>(`${MATCHING_URL}/score`, payload);
    return res.data.data;
  },

  calculateMatchScoreWithStrategy: async (
    payload: MatchScoreRequest,
    strategy: string
  ): Promise<MatchScoreResult> => {
    const res = await api.post<ApiResponse<MatchScoreResult>>(
      `${MATCHING_URL}/score/${strategy}`,
      payload
    );
    return res.data.data;
  },
};
