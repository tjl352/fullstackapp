export interface IEbook {
  id: number;
  format: string;
  title?: string | null;
  price?: number | null;
}

export interface IEbookOrderItem {
  id: number;
  ebookId: number;
  format: string;
  title?: string | null;
  quantity: number;
  unitPrice?: number | null;
}

export interface IEbookOrder {
  id: number;
  customerLogin: string;
  status?: string | null;
  placedAt?: string | null;
  items: IEbookOrderItem[];
}
