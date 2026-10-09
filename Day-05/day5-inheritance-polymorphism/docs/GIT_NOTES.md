# Day 5: Git Branching, Conflict Resolution & Rebase Notes

This document logs the hands-on Git operations executed during Day 5 training in our local repository. All actions were performed on real Git branches.

---

## 1. Feature Branch Creation

To keep Day 5 development isolated from stable code:
```bash
git checkout -b feature/day5-inheritance-polymorphism
```
All Day 5 classes (`BaseEntity`, `BidStrategy`, `ManualBidStrategy`, `AutoBidStrategy`, `BidEngineService`, `PaymentService`, and `Day5Demo`) were built and verified on this branch.

---

## 2. Real Merge Conflict Creation & Resolution

To safely demonstrate a real merge conflict without altering the application code, we used two temporary practice branches and a shared document (`docs/git-merge-practice.txt`).

### Step-by-step Execution:

1. **Created Branch A and modified lines 5–9:**
   ```bash
   git checkout -b git-conflict-practice-a
   # Added section for Branch A (Manual Bidding Notes)
   git add docs/git-merge-practice.txt
   git commit -m "Practice: add merge practice note from branch A"
   ```

2. **Created Branch B from the common base and modified the same lines:**
   ```bash
   git checkout feature/day5-inheritance-polymorphism
   git checkout -b git-conflict-practice-b
   # Added section for Branch B (Auto Bidding Notes) on identical lines
   git add docs/git-merge-practice.txt
   git commit -m "Practice: add merge practice note from branch B"
   ```

3. **Attempted to merge Branch A into Branch B:**
   ```bash
   git merge git-conflict-practice-a
   ```
   **Output:**
   ```text
   Auto-merging docs/git-merge-practice.txt
   CONFLICT (add/add): Merge conflict in docs/git-merge-practice.txt
   Automatic merge failed; fix conflicts and then commit the result.
   ```

4. **Inspected Conflict Markers in `docs/git-merge-practice.txt`:**
   ```text
   <<<<<<< HEAD
   ## Section 1: Merge Note from Branch B
   Branch B introduces auto/proxy bid strategy notes and ceiling budget checks.
   - Feature: Auto bidding evaluates increments up to user ceiling.
   - Status: Prepared by Branch B.
   =======
   ## Section 1: Merge Note from Branch A
   Branch A introduces manual bid strategy documentation and validation notes.
   - Feature: Manual bidding ensures strict increment validation.
   - Status: Prepared by Branch A.
   >>>>>>> git-conflict-practice-a
   ```

5. **Manual Conflict Resolution:**
   Rather than discarding either side, both perspectives were merged into a unified summary:
   ```text
   ## Section 1: Merge Note from Branch A & Branch B (Resolved)
   This section merges the insights contributed from both practice branches:
   - Feature A (Manual Bidding): Direct bidding that verifies increments against the current highest bid.
   - Feature B (Auto/Proxy Bidding): Automated bidding that increments up to a defined ceiling budget.
   - Resolution Status: Manually reviewed and integrated without losing data from either branch.
   ```

6. **Staging and Committing Resolution:**
   ```bash
   git add docs/git-merge-practice.txt
   git commit -m "Practice: resolve merge conflict between branch A and branch B"
   ```
   Verified with `git status`: working tree clean, merge successfully concluded.

---

## 3. Git Rebase Practice

Rebasing allows replaying commits from one branch onto the tip of another to maintain a clean, linear project history.

### Execution:
1. Created branch `git-rebase-practice` from `feature/day5-inheritance-polymorphism`.
2. Committed a documentation file (`docs/git-rebase-practice.md`):
   - Commit: `Practice: add rebase documentation note`
3. Meanwhile, added an architecture note commit on `feature/day5-inheritance-polymorphism` (`docs: add Day 5 architecture notes on feature branch`).
4. Rebased `git-rebase-practice` onto `feature/day5-inheritance-polymorphism`:
   ```bash
   git checkout git-rebase-practice
   git rebase feature/day5-inheritance-polymorphism
   ```
   **Result:**
   ```text
   Rebasing (1/1)
   Successfully rebased and updated refs/heads/git-rebase-practice.
   ```
5. **History Verification (`git log --oneline`):**
   The commit from `git-rebase-practice` was smoothly placed on top of the latest feature branch commit without creating an extra merge bubble.

---

## 4. Key Lessons Learned

- **Merge vs. Rebase:** `git merge` preserves original commit history and timestamps with a merge commit, while `git rebase` rewrites commit history linearly. Never rebase public/shared branches that others are working on.
- **Handling Conflicts:** Merge conflicts occur when Git cannot determine which version to keep. Inspecting conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`) and manually editing the file ensures no critical changes are lost.
