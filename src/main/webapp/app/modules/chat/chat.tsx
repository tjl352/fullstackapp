import './chat.scss';

import React, { useEffect, useRef, useState } from 'react';
import { Alert, Button, Col, Form, Row, Spinner } from 'reactstrap';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { fetchChatHistory, reset, sendChatMessage } from './chat.reducer';

export const ChatPage = () => {
  const dispatch = useAppDispatch();
  const { messages, loading, sending, errorMessage } = useAppSelector(state => state.chat);
  const [input, setInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    dispatch(fetchChatHistory());
    return () => {
      dispatch(reset());
    };
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, sending]);

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    const trimmed = input.trim();
    if (!trimmed || sending) {
      return;
    }
    setInput('');
    dispatch(sendChatMessage(trimmed));
  };

  return (
    <div className="chat-page">
      <Row className="justify-content-center">
        <Col md="12">
          <h1 data-cy="chatTitle">AI Chat</h1>
          <p className="text-muted">Ask anything. Your conversation is saved to your account.</p>
        </Col>
      </Row>

      {errorMessage && (
        <Alert color="danger" data-cy="chatError">
          {errorMessage}
        </Alert>
      )}

      <div className="chat-window">
        <div className="chat-messages" data-cy="chatMessages">
          {loading ? (
            <div className="text-center mt-4">
              <Spinner color="primary" />
            </div>
          ) : messages.length === 0 ? (
            <div className="chat-empty">No messages yet. Start the conversation below.</div>
          ) : (
            messages.map((message, index) => (
              <div key={`${message.id ?? 'local'}-${index}`} className={`chat-message ${message.role}`}>
                <div className="chat-bubble">{message.content}</div>
              </div>
            ))
          )}
          {sending && (
            <div className="chat-message assistant">
              <div className="chat-bubble">
                <Spinner size="sm" className="me-2" />
                Thinking...
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        <Form className="chat-input-row" onSubmit={handleSubmit}>
          <textarea
            className="form-control"
            rows={2}
            placeholder="Type your message..."
            value={input}
            onChange={event => setInput(event.target.value)}
            disabled={sending || loading}
            data-cy="chatInput"
          />
          <Button color="primary" type="submit" disabled={sending || loading || !input.trim()} data-cy="chatSend">
            Send
          </Button>
        </Form>
      </div>
    </div>
  );
};

export default ChatPage;
