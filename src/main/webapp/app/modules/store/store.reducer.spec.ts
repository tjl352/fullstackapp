import axios from 'axios';
import sinon from 'sinon';
import { configureStore } from '@reduxjs/toolkit';

import { IEbook } from 'app/shared/model/ebook.model';
import reducer, { addToCart, checkout, deleteOrder, fetchEbooks, removeFromCart, updateOrder } from './store.reducer';

const notes: IEbook = { id: 101, format: 'PDF', title: 'North Sea Field Notes', price: 12 };
const ledger: IEbook = { id: 102, format: 'WORD', title: 'Kitchen Ledger', price: null };

describe('Store reducer', () => {
  const initial = {
    loading: false,
    checkingOut: false,
    orderConfirmed: false,
    savingOrder: false,
    errorMessage: null,
    ebooks: [],
    orders: [],
    cart: [],
  };

  it('adds an ebook once', () => {
    const withOne = reducer(initial, addToCart(notes));
    const withSame = reducer(withOne, addToCart(notes));

    expect(withSame.cart).toEqual([notes]);
  });

  it('removes an ebook from the cart', () => {
    const withTwo = reducer({ ...initial, cart: [notes, ledger] }, removeFromCart(101));

    expect(withTwo.cart).toEqual([ledger]);
  });

  it('stores the ebooks returned by the API', () => {
    const state = reducer(initial, { type: fetchEbooks.fulfilled.type, payload: [notes] });

    expect(state.loading).toBe(false);
    expect(state.ebooks).toEqual([notes]);
  });

  it('clears the cart when checkout succeeds', () => {
    const state = reducer({ ...initial, cart: [notes], checkingOut: true }, { type: checkout.fulfilled.type, payload: { id: 1 } });

    expect(state.orderConfirmed).toBe(true);
    expect(state.cart).toEqual([]);
    expect(state.checkingOut).toBe(false);
  });

  it('posts the cart ebook ids on checkout', async () => {
    const store = configureStore({
      reducer: (state = [], action) => [...state, action],
    });
    axios.post = sinon.stub().returns(Promise.resolve({ data: { id: 9 } }));

    await store.dispatch(checkout([101, 102]));

    expect(axios.post.calledWith('api/ebook-orders', { ebookIds: [101, 102] })).toBe(true);
  });

  it('updates one order', async () => {
    const store = configureStore({
      reducer: (state = [], action) => [...state, action],
    });
    axios.put = sinon.stub().returns(Promise.resolve({ data: { id: 201, status: 'CANCELLED', items: [] } }));

    await store.dispatch(updateOrder({ id: 201, status: 'CANCELLED' }));

    expect(axios.put.calledWith('api/ebook-orders/201', { status: 'CANCELLED' })).toBe(true);
  });

  it('deletes one order', async () => {
    const store = configureStore({
      reducer: (state = [], action) => [...state, action],
    });
    axios.delete = sinon.stub().returns(Promise.resolve({}));

    await store.dispatch(deleteOrder(201));

    expect(axios.delete.calledWith('api/ebook-orders/201')).toBe(true);
  });
});
