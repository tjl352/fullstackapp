import React from 'react';
import { Link } from 'react-router-dom';
import { Alert, Button, Col, Form, Row, Table } from 'reactstrap';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { formatPrice } from './store';
import { checkout } from './store.reducer';

export const CheckoutPage = () => {
  const dispatch = useAppDispatch();
  const { cart, checkingOut, orderConfirmed, errorMessage } = useAppSelector(state => state.store);

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (cart.length === 0 || checkingOut) {
      return;
    }
    dispatch(checkout(cart.map(item => item.id)));
  };

  return (
    <Row className="justify-content-center">
      <Col md="10">
        <h1>Checkout</h1>
        {orderConfirmed && (
          <Alert color="success" data-cy="orderConfirmed">
            Order confirmed.
          </Alert>
        )}
        {errorMessage && (
          <Alert color="danger" data-cy="checkoutError">
            {errorMessage}
          </Alert>
        )}
        {cart.length === 0 && !orderConfirmed ? (
          <Alert color="info">Your cart is empty.</Alert>
        ) : (
          cart.length > 0 && (
            <Form onSubmit={handleSubmit}>
              <Table responsive>
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Format</th>
                    <th>Price</th>
                  </tr>
                </thead>
                <tbody>
                  {cart.map(ebook => (
                    <tr key={ebook.id}>
                      <td>{ebook.title || 'Untitled'}</td>
                      <td>{ebook.format}</td>
                      <td>{formatPrice(ebook.price)}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
              <Button color="primary" type="submit" disabled={checkingOut} data-cy="confirmOrder">
                {checkingOut ? 'Placing order…' : 'Confirm order'}
              </Button>
            </Form>
          )
        )}
        <p className="mt-3">
          <Link to="/store">Back to store</Link>
        </p>
      </Col>
    </Row>
  );
};

export default CheckoutPage;
