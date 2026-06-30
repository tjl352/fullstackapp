import axios from 'axios';
import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';

import { IChatMessage } from 'app/shared/model/chat-message.model';
import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const apiUrl = 'api/chat';

const initialState = {
  loading: false,
  sending: false,
  errorMessage: null as string | null,
  messages: [] as IChatMessage[],
};

export type ChatState = Readonly<typeof initialState>;

export const fetchChatHistory = createAsyncThunk(
  'chat/fetch_history',
  async () => {
    const response = await axios.get<IChatMessage[]>(`${apiUrl}/history`);
    return response.data;
  },
  { serializeError: serializeAxiosError },
);

export const sendChatMessage = createAsyncThunk(
  'chat/send_message',
  async (message: string) => {
    const response = await axios.post<string>(`${apiUrl}/send`, { message }, { responseType: 'text' });
    return { prompt: message, response: response.data };
  },
  { serializeError: serializeAxiosError },
);

export const ChatSlice = createSlice({
  name: 'chat',
  initialState: initialState as ChatState,
  reducers: {
    reset() {
      return initialState;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(fetchChatHistory.pending, state => {
        state.loading = true;
        state.errorMessage = null;
      })
      .addCase(fetchChatHistory.fulfilled, (state, action) => {
        state.loading = false;
        state.messages = action.payload;
      })
      .addCase(fetchChatHistory.rejected, state => {
        state.loading = false;
        state.errorMessage = 'Could not load chat history.';
      })
      .addCase(sendChatMessage.pending, state => {
        state.sending = true;
        state.errorMessage = null;
      })
      .addCase(sendChatMessage.fulfilled, (state, action) => {
        state.sending = false;
        state.messages = [
          ...state.messages,
          { role: 'user', content: action.payload.prompt },
          { role: 'assistant', content: action.payload.response },
        ];
      })
      .addCase(sendChatMessage.rejected, state => {
        state.sending = false;
        state.errorMessage = 'The assistant could not respond. Please try again.';
      });
  },
});

export const { reset } = ChatSlice.actions;

export default ChatSlice.reducer;
