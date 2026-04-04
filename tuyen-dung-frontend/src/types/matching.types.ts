export interface CandidateSuggestion {
  ungVienId: number;
  hoTen: string;
  email: string;
  soDienThoai?: string;
  cvId: number;
  tieuDeCv: string;
  matchScore: number;
  matchPercentage: number;
  matchLevel: string;
  skillMatchCount: number;
  totalSkillsRequired: number;
  calculatedAt: number;
}

export interface JobSuggestion {
  jobId: number;
  tenVitri: string;
  moTa?: string;
  congTyId?: string;
  congTyName: string;
  matchScore: number;
  matchPercentage: number;
  matchLevel: string;
  skillMatchCount: number;
  totalSkillsInCv: number;
  diaDiem?: string;
  calculatedAt: number;
}

export interface MatchScoreResult {
  cvId: number;
  jobId: number;
  matchScore: number;
  matchPercentage: number;
  matchLevel: string;
  matchedSkills: string[];
  missingSkills: string[];
  calculatedAt: number;
}

export interface MatchScoreRequest {
  cvId: number;
  jobId: number;
}
