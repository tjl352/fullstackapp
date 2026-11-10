import axios from 'axios';
import { PayloadAction, createAsyncThunk, createSlice } from '@reduxjs/toolkit';

import { IEbook, IEbookOrder } from 'app/shared/model/ebook.model';
import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const apiUrl = 'api';

const initialState = {
  loading: false,
  checkingOut: false,
  orderConfirmed: false,
  savingOrder: false,
  errorMessage: null as string | null,
  ebooks: [] as IEbook[],
  orders: [] as IEbookOrder[],
  cart: [] as IEbook[],
};

export type StoreState = Readonly<typeof initialState>;

export const fetchEbooks = createAsyncThunk(
  'store/fetch_ebooks',
  async () => {
    const response = await axios.get<IEbook[]>(`${apiUrl}/ebooks`);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const fetchOrders = createAsyncThunk(
  'store/fetch_orders',
  async () => {
    const response = await axios.get<IEbookOrder[]>(`${apiUrl}/ebook-orders`);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const updateOrder = createAsyncThunk(
  'store/update_order',
  async ({ id, status }: { id: number; status: string }) => {
    const response = await axios.put<IEbookOrder>(`${apiUrl}/ebook-orders/${id}`, { status });
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const deleteOrder = createAsyncThunk(
  'store/delete_order',
  async (id: number) => {
    await axios.delete(`${apiUrl}/ebook-orders/${id}`);
    return id;
  },
  { serializeError: serializeAxiosError },
);

export const checkout = createAsyncThunk(
  'store/checkout',
  async (ebookIds: number[]) => {
    const response = await axios.post(`${apiUrl}/ebook-orders`, { ebookIds });
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const StoreSlice = createSlice({
  name: 'store',
  initialState: initialState as StoreState,
  reducers: {
    addToCart(state, action: PayloadAction<IEbook>) {
      if (!state.cart.some(item => item.id === action.payload.id)) {
        state.cart.push(action.payload);
      }
    },
    removeFromCart(state, action: PayloadAction<number>) {
      state.cart = state.cart.filter(item => item.id !== action.payload);
    },
  },
  extraReducers(builder) {
    builder
      .addCase(fetchEbooks.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(fetchEbooks.fulfilled, (state, action) => {
        state.loading = false;
        state.ebooks = action.payload;
      })
      .addCase(fetchEbooks.rejected, state => {
        state.loading = false;
        state.errorMessage = 'Could not load the store.';
      })
      .addCase(fetchOrders.fulfilled, (state, action) => {
        state.orders = action.payload;
      })
      .addCase(fetchOrders.rejected, state => {
        state.errorMessage = 'Could not load order history.';
      })
      .addCase(updateOrder.pending, state => {
        state.savingOrder = true;
        state.errorMessage = null;
      })
      .addCase(updateOrder.fulfilled, (state, action) => {
        state.savingOrder = false;
        state.orders = state.orders.map(order => (order.id === action.payload.id ? action.payload : order));
      })
      .addCase(updateOrder.rejected, state => {
        state.savingOrder = false;
        state.errorMessage = 'Could not update the order.';
      })
      .addCase(deleteOrder.pending, state => {
        state.savingOrder = true;
        state.errorMessage = null;
      })
      .addCase(deleteOrder.fulfilled, (state, action) => {
        state.savingOrder = false;
        state.orders = state.orders.filter(order => order.id !== action.payload);
      })
      .addCase(deleteOrder.rejected, state => {
        state.savingOrder = false;
        state.errorMessage = 'Could not delete the order.';
      })
      .addCase(checkout.pending, state => {
        state.checkingOut = true;
        state.orderConfirmed = false;
        state.errorMessage = null;
      })
      .addCase(checkout.fulfilled, state => {
        state.checkingOut = false;
        state.orderConfirmed = true;
        state.cart = [];
      })
      .addCase(checkout.rejected, state => {
        state.checkingOut = false;
        state.errorMessage = 'Could not place the order. An item may no longer be available.';
      });
  },
});

export const { addToCart, removeFromCart } = StoreSlice.actions;

export default StoreSlice.reducer;
