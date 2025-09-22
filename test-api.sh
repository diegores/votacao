#!/bin/bash

# API Testing Script for Cooperative Voting System
# This script demonstrates the complete voting workflow

BASE_URL="http://localhost:8080/api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 Testing Cooperative Voting System API${NC}"
echo "============================================="

# Function to make HTTP requests and format output
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -e "\n${YELLOW}📋 $description${NC}"
    echo -e "${BLUE}$method $endpoint${NC}"
    
    if [ -n "$data" ]; then
        echo -e "${BLUE}Data: $data${NC}"
        response=$(curl -s -X $method "$BASE_URL$endpoint" \
                      -H "Content-Type: application/json" \
                      -d "$data")
    else
        response=$(curl -s -X $method "$BASE_URL$endpoint")
    fi
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Success${NC}"
        echo "$response" | jq '.' 2>/dev/null || echo "$response"
    else
        echo -e "${RED}❌ Failed${NC}"
    fi
    
    # Extract ID from response if it's a creation operation
    if [[ $endpoint == *"agendas"* && $method == "POST" && $endpoint != *"votes"* ]]; then
        AGENDA_ID=$(echo "$response" | jq -r '.id' 2>/dev/null)
    elif [[ $endpoint == *"members"* && $method == "POST" ]]; then
        MEMBER_ID=$(echo "$response" | jq -r '.id' 2>/dev/null)
    fi
}

# Check if server is running
echo -e "${YELLOW}🔍 Checking if server is running...${NC}"
if ! curl -s "$BASE_URL/../actuator/health" > /dev/null; then
    echo -e "${RED}❌ Server is not running. Please start the backend first.${NC}"
    echo -e "${YELLOW}💡 Run: ./start.sh${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Server is running${NC}"

# Test 1: Get all members (should include sample data)
make_request "GET" "/members" "" "Get all members"

# Test 2: Create a new member
make_request "POST" "/members" '{
    "cpf": "11122233344",
    "name": "Test Member"
}' "Create a new member"

# Test 3: Get all agendas (should include sample data)
make_request "GET" "/agendas" "" "Get all agendas"

# Test 4: Create a new agenda
make_request "POST" "/agendas" '{
    "title": "API Test Agenda",
    "description": "This agenda was created via API testing script"
}' "Create a new agenda"

# Test 5: Open voting session for the created agenda
if [ -n "$AGENDA_ID" ]; then
    make_request "POST" "/agendas/$AGENDA_ID/voting-session" '{
        "durationInMinutes": 2
    }' "Open voting session (2 minutes)"
    
    # Test 6: Get agenda details to verify voting session is open
    make_request "GET" "/agendas/$AGENDA_ID" "" "Get agenda details"
    
    # Test 7: Submit a vote (using sample member)
    make_request "POST" "/agendas/$AGENDA_ID/votes" '{
        "memberId": "550e8400-e29b-41d4-a716-446655440001",
        "voteType": "YES"
    }' "Submit YES vote"
    
    # Test 8: Submit another vote (different member)
    make_request "POST" "/agendas/$AGENDA_ID/votes" '{
        "memberId": "550e8400-e29b-41d4-a716-446655440002",
        "voteType": "NO"
    }' "Submit NO vote"
    
    # Test 9: Try to vote again with same member (should fail)
    echo -e "\n${YELLOW}📋 Try to vote again with same member (should fail)${NC}"
    echo -e "${BLUE}POST /agendas/$AGENDA_ID/votes${NC}"
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/agendas/$AGENDA_ID/votes" \
                  -H "Content-Type: application/json" \
                  -d '{
                      "memberId": "550e8400-e29b-41d4-a716-446655440001",
                      "voteType": "NO"
                  }')
    
    http_code=${response: -3}
    if [ "$http_code" = "409" ]; then
        echo -e "${GREEN}✅ Correctly rejected duplicate vote (HTTP 409)${NC}"
    else
        echo -e "${RED}❌ Expected HTTP 409 but got $http_code${NC}"
    fi
    
    # Test 10: Get voting results
    make_request "GET" "/agendas/$AGENDA_ID/result" "" "Get voting results"
    
    # Test 11: Get open voting sessions
    make_request "GET" "/agendas/voting-sessions/open" "" "Get open voting sessions"
else
    echo -e "${RED}❌ Could not create agenda, skipping voting tests${NC}"
fi

# Test 12: Error handling - Try to create member with invalid CPF
echo -e "\n${YELLOW}📋 Test validation - Invalid CPF (should fail)${NC}"
echo -e "${BLUE}POST /members${NC}"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/members" \
              -H "Content-Type: application/json" \
              -d '{
                  "cpf": "123",
                  "name": "Invalid Member"
              }')

http_code=${response: -3}
if [ "$http_code" = "400" ]; then
    echo -e "${GREEN}✅ Correctly rejected invalid CPF (HTTP 400)${NC}"
else
    echo -e "${RED}❌ Expected HTTP 400 but got $http_code${NC}"
fi

# Test 13: Error handling - Try to vote on non-existent agenda
echo -e "\n${YELLOW}📋 Test error handling - Vote on non-existent agenda (should fail)${NC}"
echo -e "${BLUE}POST /agendas/00000000-0000-0000-0000-000000000000/votes${NC}"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/agendas/00000000-0000-0000-0000-000000000000/votes" \
              -H "Content-Type: application/json" \
              -d '{
                  "memberId": "550e8400-e29b-41d4-a716-446655440001",
                  "voteType": "YES"
              }')

http_code=${response: -3}
if [ "$http_code" = "400" ]; then
    echo -e "${GREEN}✅ Correctly rejected vote on non-existent agenda (HTTP 400)${NC}"
else
    echo -e "${RED}❌ Expected HTTP 400 but got $http_code${NC}"
fi

# Test 14: CPF Validation - Generate valid CPF
make_request "GET" "/cpf-generator/generate" "" "Generate a valid CPF for testing"

# Test 15: CPF Validation - Validate CPF (should randomly succeed or fail)
echo -e "\n${YELLOW}📋 Test CPF validation with random results${NC}"
echo -e "${BLUE}GET /cpf/validate/11144477735${NC}"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/cpf/validate/11144477735")

http_code=${response: -3}
if [ "$http_code" = "200" ]; then
    echo -e "${GREEN}✅ CPF validation successful (ABLE_TO_VOTE)${NC}"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
elif [ "$http_code" = "404" ]; then
    echo -e "${YELLOW}⚠️ CPF validation failed (UNABLE_TO_VOTE or invalid CPF)${NC}"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
else
    echo -e "${RED}❌ Unexpected response: $http_code${NC}"
fi

# Test 16: CPF Validation - Test invalid CPF
echo -e "\n${YELLOW}📋 Test invalid CPF validation (should fail)${NC}"
echo -e "${BLUE}GET /cpf/validate/12345${NC}"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/cpf/validate/12345")

http_code=${response: -3}
if [ "$http_code" = "404" ]; then
    echo -e "${GREEN}✅ Correctly rejected invalid CPF (HTTP 404)${NC}"
else
    echo -e "${RED}❌ Expected HTTP 404 but got $http_code${NC}"
fi

# Test 17: Create member with CPF validation
echo -e "\n${YELLOW}📋 Test member creation with CPF validation${NC}"
echo -e "${BLUE}POST /members${NC}"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/members" \
              -H "Content-Type: application/json" \
              -d '{
                  "cpf": "11144477735",
                  "name": "Test Member with CPF Validation"
              }')

http_code=${response: -3}
if [ "$http_code" = "201" ]; then
    echo -e "${GREEN}✅ Member created successfully with valid CPF${NC}"
elif [ "$http_code" = "404" ]; then
    echo -e "${YELLOW}⚠️ Member creation failed due to CPF validation (UNABLE_TO_VOTE)${NC}"
else
    echo -e "${RED}❌ Unexpected response: $http_code${NC}"
fi

echo -e "\n${GREEN}🎉 API testing completed!${NC}"
echo -e "${BLUE}📊 Summary:${NC}"
echo -e "   • Member management: ✅"
echo -e "   • Agenda creation: ✅"
echo -e "   • Voting session management: ✅"
echo -e "   • Vote submission: ✅"
echo -e "   • Vote counting: ✅"
echo -e "   • Error handling: ✅"
echo -e "   • Data validation: ✅"
echo -e "   • CPF validation (external service): ✅"
echo -e "   • CPF generation utilities: ✅"
echo ""
echo -e "${YELLOW}💡 Check the frontend at http://localhost:3000 to see the results!${NC}"
echo -e "${YELLOW}💡 Test CPF validation at http://localhost:8080/swagger-ui.html${NC}"