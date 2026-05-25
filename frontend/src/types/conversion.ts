export type ConversionStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED';

export interface ConversionTask {
  id: number;
  sourceTrackId: number;
  sourceFormat: string | null;
  targetFormat: string;
  targetBitrate: number | null;
  targetSampleRate: number | null;
  status: ConversionStatus;
  progress: number;
  outputPath: string | null;
  errorMessage: string | null;
  createdAt: string;
  startedAt: string | null;
  finishedAt: string | null;
}

export interface ConversionSubmit {
  sourceTrackId: number;
  targetFormat: string;
  targetBitrate?: number;
  targetSampleRate?: number;
}
