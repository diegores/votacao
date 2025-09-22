#!/bin/bash

echo "🛑 Stopping Cooperative Voting System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Stop backend
if [ -f "backend.pid" ]; then
    BACKEND_PID=$(cat backend.pid)
    if ps -p $BACKEND_PID > /dev/null; then
        echo -e "${YELLOW}🔌 Stopping backend (PID: $BACKEND_PID)...${NC}"
        kill $BACKEND_PID
        rm backend.pid
        echo -e "${GREEN}✅ Backend stopped${NC}"
    else
        echo -e "${YELLOW}⚠️  Backend process not found${NC}"
        rm -f backend.pid
    fi
else
    echo -e "${YELLOW}⚠️  Backend PID file not found${NC}"
fi

# Stop frontend
if [ -f "frontend.pid" ]; then
    FRONTEND_PID=$(cat frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null; then
        echo -e "${YELLOW}🌐 Stopping frontend (PID: $FRONTEND_PID)...${NC}"
        kill $FRONTEND_PID
        rm frontend.pid
        echo -e "${GREEN}✅ Frontend stopped${NC}"
    else
        echo -e "${YELLOW}⚠️  Frontend process not found${NC}"
        rm -f frontend.pid
    fi
else
    echo -e "${YELLOW}⚠️  Frontend PID file not found${NC}"
fi

# Stop any remaining processes on ports 8080 and 3000
echo -e "${YELLOW}🧹 Cleaning up any remaining processes...${NC}"

# Kill processes on port 8080 (backend)
lsof -ti:8080 | xargs kill -9 2>/dev/null && echo -e "${GREEN}✅ Cleaned up port 8080${NC}" || echo -e "${YELLOW}⚠️  No processes found on port 8080${NC}"

# Kill processes on port 3000 (frontend)
lsof -ti:3000 | xargs kill -9 2>/dev/null && echo -e "${GREEN}✅ Cleaned up port 3000${NC}" || echo -e "${YELLOW}⚠️  No processes found on port 3000${NC}"

echo -e "${GREEN}🎉 All services stopped successfully!${NC}"