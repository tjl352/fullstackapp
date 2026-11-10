import React from 'react';
import { Link } from 'react-router-dom';
import { Alert, Button, Col, Row, Table } from 'reactstrap';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { formatPrice } from './store';
import { removeFromCart } from './store.reducer';

export const CartPage = () => {
  const dispatch = useAppDispatch();
  const cart = useAppSelector(state => state.store.cart);

  return (
    <Row className="justify-content-center">
      <Col md="10">
        <h1>Cart</h1>
        {cart.length === 0 ? (
          <Alert color="info">Your cart is empty.</Alert>
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
              {cart.map(ebook => (
                <tr key={ebook.id} data-cy="cartRow">
                  <td>{ebook.title || 'Untitled'}</td>
                  <td>{ebook.format}</td>
                  <td>{formatPrice(ebook.price)}</td>
                  <td className="text-end">
                    <Button color="danger" size="sm" outline onClick={() => dispatch(removeFromCart(ebook.id))} data-cy="removeFromCart">
                      Remove
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
        <Button tag={Link} to="/store" color="secondary" outline className="me-2">
          Back to store
        </Button>
        {cart.length > 0 && (
          <Button tag={Link} to="/store/checkout" color="primary" data-cy="goToCheckout">
            Checkout
          </Button>
        )}
      </Col>
    </Row>
  );
};

export default CartPage;
