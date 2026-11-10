import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Alert, Button, Col, Form, Input, Row, Spinner, Table } from 'reactstrap';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { IEbook } from 'app/shared/model/ebook.model';
import { addToCart, deleteOrder, fetchEbooks, fetchOrders, updateOrder } from './store.reducer';

export const formatPrice = (price?: number | null) => (price == null ? '—' : `$${Number(price).toFixed(2)}`);

export const StorePage = () => {
  const dispatch = useAppDispatch();
  const { ebooks, orders, cart, loading, savingOrder, errorMessage } = useAppSelector(state => state.store);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [status, setStatus] = useState('');

  useEffect(() => {
    dispatch(fetchEbooks());
    dispatch(fetchOrders());
  }, []);

  const selectedOrder = orders.find(order => order.id === selectedOrderId) ?? null;

  useEffect(() => {
    setStatus(selectedOrder?.status ?? '');
  }, [selectedOrderId]);

  const inCart = (ebook: IEbook) => cart.some(item => item.id === ebook.id);

  return (
    <Row className="justify-content-center">
      <Col md="10">
        <div className="d-flex justify-content-between align-items-center">
          <h1 data-cy="storeTitle">Store</h1>
          <Button tag={Link} to="/store/cart" color="primary" outline data-cy="openCart">
            Cart ({cart.length})
          </Button>
        </div>
        <p className="text-muted">Each ebook is one original copy.</p>
        {errorMessage && <Alert color="danger">{errorMessage}</Alert>}
        {loading ? (
          <Spinner color="primary" />
        ) : ebooks.length === 0 ? (
          <Alert color="info">No ebooks are for sale.</Alert>
        ) : (
          <Table responsive>
            <thead>
              <tr>
                <th>Title</th>
                <th>Format</th>
                <th>Price</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {ebooks.map(ebook => (
                <tr key={ebook.id} data-cy="ebookRow">
                  <td>{ebook.title || 'Untitled'}</td>
                  <td>{ebook.format}</td>
                  <td>{formatPrice(ebook.price)}</td>
                  <td className="text-end">
                    <Button
                      color="primary"
                      size="sm"
                      disabled={inCart(ebook)}
                      onClick={() => dispatch(addToCart(ebook))}
                      data-cy="addToCart"
                    >
                      {inCart(ebook) ? 'In cart' : 'Add to cart'}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}

        <h2 className="mt-4">Order history</h2>
        {orders.length === 0 ? (
          <Alert color="info">No orders yet.</Alert>
        ) : (
          <>
            <Table responsive>
              <thead>
                <tr>
                  <th />
                  <th>Order</th>
                  <th>Customer</th>
                  <th>Status</th>
                  <th>Copies</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => (
                  <tr key={order.id} data-cy="orderRow">
                    <td>
                      <input
                        type="radio"
                        name="selectedOrder"
                        checked={selectedOrderId === order.id}
                        onChange={() => setSelectedOrderId(order.id)}
                        aria-label={`Select order ${order.id}`}
                      />
                    </td>
                    <td>{order.id}</td>
                    <td>{order.customerLogin}</td>
                    <td>{order.status || '—'}</td>
                    <td>{order.items.map(item => item.title || 'Untitled').join(', ')}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
            <Form
              className="d-flex flex-wrap gap-2 align-items-center"
              onSubmit={event => {
                event.preventDefault();
                if (selectedOrderId == null || savingOrder) {
                  return;
                }
                dispatch(updateOrder({ id: selectedOrderId, status }));
              }}
            >
              <Input
                type="text"
                value={status}
                maxLength={20}
                placeholder="Status"
                aria-label="Order status"
                disabled={selectedOrderId == null || savingOrder}
                onChange={event => setStatus(event.target.value)}
                style={{ maxWidth: '12rem' }}
              />
              <Button color="primary" type="submit" disabled={selectedOrderId == null || savingOrder} data-cy="updateOrder">
                Update order
              </Button>
              <Button
                color="danger"
                type="button"
                outline
                disabled={selectedOrderId == null || savingOrder}
                data-cy="deleteOrder"
                onClick={() => {
                  if (selectedOrderId == null) {
                    return;
                  }
                  dispatch(deleteOrder(selectedOrderId))
                    .unwrap()
                    .then(() => {
                      setSelectedOrderId(null);
                      dispatch(fetchEbooks());
                    })
                    .catch(() => undefined);
                }}
              >
                Delete order
              </Button>
            </Form>
            <p className="text-muted mt-2">Select one order to update its status or delete it.</p>
          </>
        )}
      </Col>
    </Row>
  );
};

export default StorePage;
