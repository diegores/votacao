#!/usr/bin/env python3
"""
Performance Test Data Generator for Voting System
Generates test data for high-volume voting scenarios
"""

import json
import uuid
import requests
import time
import random
from concurrent.futures import ThreadPoolExecutor, as_completed
import argparse

class VotingTestDataGenerator:
    def __init__(self, base_url="http://localhost:8080"):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})
    
    def create_agenda(self, title="Load Test Agenda", description="Performance testing agenda"):
        """Create a test agenda"""
        data = {
            "title": title,
            "description": description
        }
        response = self.session.post(f"{self.base_url}/api/agendas", json=data)
        response.raise_for_status()
        return response.json()["id"]
    
    def open_voting_session(self, agenda_id, duration_minutes=60):
        """Open voting session for agenda"""
        data = {"durationInMinutes": duration_minutes}
        response = self.session.post(
            f"{self.base_url}/api/agendas/{agenda_id}/voting-session", 
            json=data
        )
        response.raise_for_status()
        return response.json()
    
    def create_member(self, cpf, name):
        """Create a test member"""
        data = {"cpf": cpf, "name": name}
        try:
            response = self.session.post(f"{self.base_url}/api/members", json=data)
            response.raise_for_status()
            return response.json()["id"]
        except requests.exceptions.HTTPError as e:
            if e.response.status_code == 404:  # CPF validation failed
                return None
            raise
    
    def generate_valid_cpf(self):
        """Generate a valid Brazilian CPF for testing"""
        response = self.session.get(f"{self.base_url}/api/cpf-generator/generate")
        response.raise_for_status()
        return response.json()["cpf"]
    
    def submit_vote(self, agenda_id, member_id, vote_type="YES"):
        """Submit a single vote"""
        data = {
            "memberId": member_id,
            "voteType": vote_type
        }
        response = self.session.post(
            f"{self.base_url}/api/agendas/{agenda_id}/votes", 
            json=data
        )
        return response.status_code == 201
    
    def submit_batch_votes(self, agenda_id, votes):
        """Submit batch votes"""
        data = {
            "agendaId": agenda_id,
            "votes": votes
        }
        start_time = time.time()
        response = self.session.post(f"{self.base_url}/api/batch-voting/votes", json=data)
        end_time = time.time()
        
        result = {
            "success": response.status_code in [200, 206],
            "processing_time": end_time - start_time,
            "batch_size": len(votes)
        }
        
        if result["success"]:
            result.update(response.json())
        
        return result
    
    def create_test_members(self, count=10000):
        """Create test members for voting"""
        print(f"Creating {count} test members...")
        members = []
        
        with ThreadPoolExecutor(max_workers=50) as executor:
            futures = []
            
            for i in range(count):
                cpf = self.generate_valid_cpf()
                name = f"Test Member {i+1}"
                future = executor.submit(self.create_member, cpf, name)
                futures.append(future)
            
            for i, future in enumerate(as_completed(futures)):
                member_id = future.result()
                if member_id:
                    members.append(member_id)
                
                if (i + 1) % 1000 == 0:
                    print(f"Created {i + 1} members...")
        
        print(f"Successfully created {len(members)} members")
        return members
    
    def run_individual_vote_test(self, agenda_id, members, votes_per_member=1):
        """Test individual vote submission performance"""
        print(f"Running individual vote test with {len(members)} members...")
        
        votes_submitted = 0
        failed_votes = 0
        start_time = time.time()
        
        with ThreadPoolExecutor(max_workers=100) as executor:
            futures = []
            
            for member_id in members:
                for _ in range(votes_per_member):
                    vote_type = random.choice(["YES", "NO"])
                    future = executor.submit(self.submit_vote, agenda_id, member_id, vote_type)
                    futures.append(future)
            
            for future in as_completed(futures):
                if future.result():
                    votes_submitted += 1
                else:
                    failed_votes += 1
                
                if (votes_submitted + failed_votes) % 1000 == 0:
                    print(f"Processed {votes_submitted + failed_votes} votes...")
        
        end_time = time.time()
        total_time = end_time - start_time
        
        return {
            "votes_submitted": votes_submitted,
            "failed_votes": failed_votes,
            "total_time": total_time,
            "votes_per_second": votes_submitted / total_time if total_time > 0 else 0
        }
    
    def run_batch_vote_test(self, agenda_id, member_count=100000, batch_size=1000):
        """Test batch vote submission performance"""
        print(f"Running batch vote test with {member_count} votes in batches of {batch_size}...")
        
        batches_processed = 0
        total_successful_votes = 0
        total_failed_votes = 0
        total_processing_time = 0
        start_time = time.time()
        
        # Generate votes in batches
        for batch_start in range(0, member_count, batch_size):
            batch_end = min(batch_start + batch_size, member_count)
            current_batch_size = batch_end - batch_start
            
            # Generate batch votes
            votes = []
            for _ in range(current_batch_size):
                votes.append({
                    "memberId": str(uuid.uuid4()),
                    "voteType": random.choice(["YES", "NO"])
                })
            
            # Submit batch
            result = self.submit_batch_votes(agenda_id, votes)
            
            if result["success"]:
                batches_processed += 1
                total_successful_votes += result.get("successfulVotes", 0)
                total_failed_votes += result.get("failedVotes", 0)
                total_processing_time += result["processing_time"]
            
            if batches_processed % 10 == 0:
                print(f"Processed {batches_processed} batches...")
        
        end_time = time.time()
        total_time = end_time - start_time
        
        return {
            "batches_processed": batches_processed,
            "total_successful_votes": total_successful_votes,
            "total_failed_votes": total_failed_votes,
            "total_time": total_time,
            "avg_batch_processing_time": total_processing_time / batches_processed if batches_processed > 0 else 0,
            "votes_per_second": total_successful_votes / total_time if total_time > 0 else 0
        }

def main():
    parser = argparse.ArgumentParser(description="Voting System Performance Test")
    parser.add_argument("--base-url", default="http://localhost:8080", 
                       help="Base URL of the voting system")
    parser.add_argument("--test-type", choices=["individual", "batch", "both"], 
                       default="both", help="Type of performance test to run")
    parser.add_argument("--member-count", type=int, default=10000, 
                       help="Number of members/votes to test")
    parser.add_argument("--batch-size", type=int, default=1000, 
                       help="Batch size for batch voting tests")
    
    args = parser.parse_args()
    
    generator = VotingTestDataGenerator(args.base_url)
    
    print("=== Voting System Performance Test ===")
    print(f"Base URL: {args.base_url}")
    print(f"Test Type: {args.test_type}")
    print(f"Member/Vote Count: {args.member_count}")
    print(f"Batch Size: {args.batch_size}")
    print()
    
    # Create agenda and open voting session
    print("Setting up test environment...")
    agenda_id = generator.create_agenda("Performance Test Agenda")
    generator.open_voting_session(agenda_id, 120)  # 2 hours
    print(f"Created agenda: {agenda_id}")
    print()
    
    results = {}
    
    if args.test_type in ["individual", "both"]:
        print("=== Individual Vote Performance Test ===")
        members = generator.create_test_members(args.member_count)
        individual_results = generator.run_individual_vote_test(agenda_id, members)
        results["individual"] = individual_results
        
        print(f"Individual Vote Results:")
        print(f"  Votes Submitted: {individual_results['votes_submitted']}")
        print(f"  Failed Votes: {individual_results['failed_votes']}")
        print(f"  Total Time: {individual_results['total_time']:.2f} seconds")
        print(f"  Votes/Second: {individual_results['votes_per_second']:.2f}")
        print()
    
    if args.test_type in ["batch", "both"]:
        print("=== Batch Vote Performance Test ===")
        batch_results = generator.run_batch_vote_test(agenda_id, args.member_count, args.batch_size)
        results["batch"] = batch_results
        
        print(f"Batch Vote Results:")
        print(f"  Batches Processed: {batch_results['batches_processed']}")
        print(f"  Successful Votes: {batch_results['total_successful_votes']}")
        print(f"  Failed Votes: {batch_results['total_failed_votes']}")
        print(f"  Total Time: {batch_results['total_time']:.2f} seconds")
        print(f"  Avg Batch Processing Time: {batch_results['avg_batch_processing_time']:.2f} seconds")
        print(f"  Votes/Second: {batch_results['votes_per_second']:.2f}")
        print()
    
    # Save results
    with open("performance-tests/results/performance-test-results.json", "w") as f:
        json.dump(results, f, indent=2)
    
    print("Performance test completed. Results saved to performance-test-results.json")

if __name__ == "__main__":
    main()