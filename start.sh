#!/bin/bash

echo "🚀 Starting Cooperative Voting System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if Java 21 is installed
check_java() {
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
        if [ "$JAVA_VERSION" -ge 21 ]; then
            echo -e "${GREEN}✅ Java $JAVA_VERSION found${NC}"
        else
            echo -e "${RED}❌ Java 21+ required. Current version: $JAVA_VERSION${NC}"
            exit 1
        fi
    else
        echo -e "${RED}❌ Java not found. Please install Java 21+${NC}"
        exit 1
    fi
}

# Check if Maven is installed
check_maven() {
    if command -v mvn &> /dev/null; then
        echo -e "${GREEN}✅ Maven found${NC}"
    else
        echo -e "${RED}❌ Maven not found. Please install Maven 3.8+${NC}"
        exit 1
    fi
}

# Check if Node.js is installed
check_node() {
    if command -v node &> /dev/null; then
        NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
        if [ "$NODE_VERSION" -ge 18 ]; then
            echo -e "${GREEN}✅ Node.js v$(node -v) found${NC}"
        else
            echo -e "${YELLOW}⚠️  Node.js 18+ recommended. Current version: v$(node -v)${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Node.js not found. Frontend won't be available.${NC}"
        FRONTEND_AVAILABLE=false
    fi
}

# Build and start backend
start_backend() {
    echo -e "${BLUE}🔨 Building backend...${NC}"
    mvn clean install -q
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Backend built successfully${NC}"
        echo -e "${BLUE}🚀 Starting backend on port 8080...${NC}"
        
        # Start backend in background
        nohup mvn spring-boot:run > backend.log 2>&1 &
        BACKEND_PID=$!
        echo $BACKEND_PID > backend.pid
        
        # Wait for backend to start
        echo -e "${YELLOW}⏳ Waiting for backend to start...${NC}"
        for i in {1..30}; do
            if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
                echo -e "${GREEN}✅ Backend started successfully${NC}"
                break
            fi
            sleep 2
            if [ $i -eq 30 ]; then
                echo -e "${RED}❌ Backend failed to start${NC}"
                exit 1
            fi
        done
    else
        echo -e "${RED}❌ Backend build failed${NC}"
        exit 1
    fi
}

# Start frontend
start_frontend() {
    if [ "$FRONTEND_AVAILABLE" != false ]; then
        echo -e "${BLUE}🔨 Setting up frontend...${NC}"
        cd frontend
        
        if [ ! -d "node_modules" ]; then
            echo -e "${YELLOW}📦 Installing frontend dependencies...${NC}"
            npm install --silent
        fi
        
        echo -e "${BLUE}🚀 Starting frontend on port 3000...${NC}"
        npm start > ../frontend.log 2>&1 &
        FRONTEND_PID=$!
        echo $FRONTEND_PID > ../frontend.pid
        cd ..
        
        echo -e "${GREEN}✅ Frontend starting in background${NC}"
    fi
}

# Display access information
show_access_info() {
    echo ""
    echo -e "${GREEN}🎉 Cooperative Voting System is starting up!${NC}"
    echo ""
    echo -e "${BLUE}📍 Access Points:${NC}"
    echo -e "   🌐 Frontend Application: ${GREEN}http://localhost:3000${NC}"
    echo -e "   🔌 Backend API: ${GREEN}http://localhost:8080${NC}"
    echo -e "   📚 API Documentation: ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
    echo -e "   🗄️  H2 Database Console: ${GREEN}http://localhost:8080/h2-console${NC}"
    echo ""
    echo -e "${YELLOW}💡 Database Connection:${NC}"
    echo -e "   JDBC URL: ${GREEN}jdbc:h2:file:./data/votacao${NC}"
    echo -e "   Username: ${GREEN}sa${NC}"
    echo -e "   Password: ${GREEN}(empty)${NC}"
    echo ""
    echo -e "${BLUE}📋 Quick Commands:${NC}"
    echo -e "   Stop all: ${GREEN}./stop.sh${NC}"
    echo -e "   View logs: ${GREEN}tail -f backend.log${NC} or ${GREEN}tail -f frontend.log${NC}"
    echo ""
    echo -e "${GREEN}✨ Happy voting!${NC}"
}

# Main execution
main() {
    echo -e "${BLUE}🔍 Checking prerequisites...${NC}"
    check_java
    check_maven
    check_node
    
    echo ""
    start_backend
    
    if [ "$FRONTEND_AVAILABLE" != false ]; then
        start_frontend
    fi
    
    show_access_info
}

# Set frontend availability flag
FRONTEND_AVAILABLE=true

# Run main function
main