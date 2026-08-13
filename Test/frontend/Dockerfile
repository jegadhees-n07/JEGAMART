FROM node:18-alpine

WORKDIR /app

# Install http-server globally
RUN npm install -g http-server

# Copy frontend files
COPY . .

EXPOSE 3000

CMD ["http-server", "-p", "3000", "-c-1"]
