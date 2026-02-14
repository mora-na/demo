import api from "./http";

export interface ApiResponse<T> {
    code: number;
    message?: string;
    data?: T;
}

export interface PageResult<T> {
    total: number;
    data: T[];
    pageNum: number;
    pageSize: number;
}

export interface OrderVO {
    id: number;
    userId: number;
    userName?: string;
    nickName?: string;
    amount: number | string;
    remark?: string;
    createdAt?: string;
}

export interface OrderQuery {
    pageNum?: number;
    pageSize?: number;
    userId?: number;
    minAmount?: number;
    maxAmount?: number;
}

export async function listOrders(params: OrderQuery): Promise<ApiResponse<PageResult<OrderVO>>> {
    const response = await api.get<ApiResponse<PageResult<OrderVO>>>("/orders", {params});
    return response.data;
}

export async function deleteOrder(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/orders/${id}`);
    return response.data;
}
