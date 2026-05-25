export interface Tag {
  id: number;
  name: string;
  color: string | null;
}

export interface TagCreate {
  name: string;
  color?: string;
}
