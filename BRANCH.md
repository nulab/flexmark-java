# Branch Strategy / ブランチ戦略

## English

### Branch Overview

This repository maintains a fork of the upstream flexmark-java project with the following branch structure:

- **`master`**: Internal development branch
  - Contains internal deployment configurations and customizations
  - Used for internal features and modifications
  - **NEVER create PRs from this branch to the upstream repository**

- **`upstream/master`**: Upstream tracking branch
  - Maintains exact synchronization with the upstream (fork source) repository
  - **Direct pushes are PROHIBITED**
  - **Creating/merging PRs within this repository is PROHIBITED**
  - **ONLY permitted operation**: Pulling changes from the upstream repository

### Workflow

#### 1. Initial Setup

Set up the upstream remote (one-time setup):

```bash
# Add upstream remote
git remote add upstream git@github.com:vsch/flexmark-java.git

# Verify remotes
git remote -v
# origin    git@github.com:nulab/flexmark-java.git (fetch)
# origin    git@github.com:nulab/flexmark-java.git (push)
# upstream  git@github.com:vsch/flexmark-java.git (fetch)
# upstream  git@github.com:vsch/flexmark-java.git (push)

# Fetch upstream branches
git fetch upstream
```

#### 2. For Internal-Only Modifications

For changes that will NOT be contributed back to upstream:

```bash
# Create feature branch from master
git checkout master
git pull origin master
git checkout -b feature/internal-feature

# Make changes and commit
git add .
git commit -m "Add internal feature"

# Push and create PR to master
git push -u origin feature/internal-feature
# Then create PR: feature/internal-feature → master on GitHub
```

#### 3. For Upstream Contributions

For changes that WILL be contributed back to upstream:

**Important**: To avoid mixing internal changes (CI/CD configurations, etc.) into upstream PRs, use the cherry-pick workflow below:

**Step 3-1: Develop on feature branch from master (internal development)**

```bash
# Create feature branch from master for internal development
git checkout master
git pull origin master
git checkout -b feature/upstream-contribution

# Make changes and commit
git add .
git commit -m "Fix: description of the fix"

# Push to your fork and create PR to master (for internal review)
git push -u origin feature/upstream-contribution
# Create PR: feature/upstream-contribution → master on GitHub
```

**Step 3-2: After internal review, merge to master**

```bash
# After PR approval, merge on GitHub
# The feature branch now contains the changes integrated into master
```

**Step 3-3: Cherry-pick for upstream contribution (avoiding internal changes)**

```bash
# Create contribution branch from upstream/master
git checkout upstream/master
git pull upstream master
git checkout -b contrib/upstream-contribution

# Cherry-pick only the commits intended for upstream
# Find the commit hash(es) from the feature branch
git log master --oneline -10  # Find target commits

# Cherry-pick the relevant commits
git cherry-pick <commit-hash1>
git cherry-pick <commit-hash2>
# Or for multiple consecutive commits: git cherry-pick <start-hash>^..<end-hash>

# Push contribution branch
git push -u origin contrib/upstream-contribution
```

**Step 3-4: Create PR to upstream repository**

```bash
# On GitHub, create PR: nulab/flexmark-java:contrib/upstream-contribution → vsch/flexmark-java:master
# This PR will ONLY contain the cherry-picked commits, without internal changes
```

**Step 3-5: After upstream PR is merged**

⚠️ **Use GitHub Actions to sync upstream/master. Do NOT use manual git commands.**

1. Go to **Actions** tab in GitHub
2. Select **"Sync upstream/master with Upstream Repository"** workflow
3. Click **"Run workflow"** → **"Run workflow"**
4. The workflow will automatically create a PR to merge changes to master
5. Review and merge the auto-created PR

See [Section 4: Upstream Synchronization](#4-upstream-synchronization) for detailed workflow instructions.

#### 4. Upstream Synchronization

Synchronize `upstream/master` with the upstream repository whenever needed (e.g., after upstream merges your contribution, or to get latest upstream changes).

**⚠️ IMPORTANT: ALL operations that push to `upstream/master` MUST use GitHub Actions. Manual git push to `upstream/master` is PROHIBITED except in emergencies.**

**Standard Method: Using GitHub Actions**

1. Navigate to **Actions** tab in GitHub
2. Select **"Sync upstream/master with Upstream Repository"** workflow from the list
3. Click **"Run workflow"** button
4. Configure options (if needed):
   - **Use workflow from**: Keep as `Branch: upstream/master`
   - **Dry run**: Check this to preview changes without creating PR (optional)
5. Click **"Run workflow"** to execute

**What happens automatically:**
- ✅ Fetches latest changes from upstream (vsch/flexmark-java)
- ✅ Updates `upstream/master` branch
- ✅ Creates sync branch: `sync/upstream-YYYYMMDD-HHMMSS`
- ✅ Creates Pull Request to `master` with:
  - Title: "🔄 Sync upstream changes (YYYY-MM-DD)"
  - Detailed commit list with authors and timestamps
  - Verification checklist

**After workflow completes:**
1. Go to **Pull Requests** tab
2. Find the auto-created PR (title starts with 🔄)
3. Review the commits and changes
4. Complete the verification checklist
5. Merge when ready

**Dry Run Mode:**
- Use when you want to preview changes first
- Check "Dry run" option before running
- Review the workflow logs to see what would be synced
- Run again without dry run to actually sync

**Emergency Manual Synchronization Only**

⚠️ **Use this ONLY when GitHub Actions is unavailable. Incorrect usage may break the branch strategy.**

```bash
# WARNING: Manual sync requires careful attention to avoid mistakes
# Update upstream/master
git checkout upstream/master
git pull upstream master
git push origin upstream/master

# Create sync branch and PR manually
git checkout -b sync/upstream-$(date +%Y%m%d)
git push -u origin sync/upstream-$(date +%Y%m%d)
# Then manually create PR: sync/upstream-YYYYMMDD → master on GitHub
```

### Important Rules

1. ✅ **Permitted Operations**
   - Create PRs from feature branches to `master`
   - Create PRs from `contrib/*` branches to upstream repository (using cherry-picked commits)
   - Create PRs from `upstream/master` to `master`
   - Pull changes from upstream to `upstream/master`

2. ❌ **PROHIBITED Operations**
   - Creating PRs from `master` to upstream repository (use cherry-pick workflow instead)
   - Creating PRs from `feature/*` branches directly to upstream (use `contrib/*` branches with cherry-picked commits)
   - **Manual pushes to `upstream/master` (MUST use GitHub Actions workflow)**
   - Creating/merging PRs targeting `upstream/master` within this repository
   - Any modifications to `upstream/master` except through GitHub Actions workflow

3. 🔑 **Key Strategy**
   - Always develop on `feature/*` branches from `master` first
   - After merging to `master`, create `contrib/*` branches from `upstream/master`
   - Use `git cherry-pick` to transfer only relevant commits to `contrib/*` branches
   - This prevents internal changes (CI/CD, etc.) from being included in upstream PRs
   - **Use GitHub Actions workflow for ALL `upstream/master` synchronization**

### Branch Protection Recommendations

Configure the following branch protection rules on GitHub:

**For `upstream/master` branch:**
- Require pull request reviews before merging: **OFF** (PRs should not be created)
- Require status checks to pass: **OFF**
- Restrict who can push: **Enable** (no one should push directly)
- Lock branch: **Consider enabling** (to prevent accidental modifications)

**For `master` branch:**
- Require pull request reviews before merging: **ON**
- Require status checks to pass: **ON** (if CI is configured)
- Dismiss stale pull request approvals: **ON**

---

## 日本語

### ブランチ概要

このリポジトリはupstream（flexmark-java）のフォークであり、以下のブランチ構成で運用します：

- **`master`**: 社内開発用ブランチ
  - 社内のデプロイ設定やカスタマイズを含む
  - 社内専用の機能追加や修正に使用
  - **このブランチからupstreamへのPRは絶対に作成しない**

- **`upstream/master`**: upstream追従ブランチ
  - Fork元（upstream）と完全に同期を保つ
  - **直接プッシュ禁止**
  - **このリポジトリ内でのPR作成・マージ禁止**
  - **許可される操作**: upstreamからのpullのみ

### ワークフロー

#### 1. 初期セットアップ

upstreamリモートの設定（初回のみ）：

```bash
# upstreamリモートを追加
git remote add upstream git@github.com:vsch/flexmark-java.git

# リモートの確認
git remote -v
# origin    git@github.com:nulab/flexmark-java.git (fetch)
# origin    git@github.com:nulab/flexmark-java.git (push)
# upstream  git@github.com:vsch/flexmark-java.git (fetch)
# upstream  git@github.com:vsch/flexmark-java.git (push)

# upstreamのブランチをフェッチ
git fetch upstream
```

#### 2. 社内専用の修正を行う場合

upstreamに出さない修正の場合：

```bash
# masterから機能ブランチを作成
git checkout master
git pull origin master
git checkout -b feature/internal-feature

# 変更をコミット
git add .
git commit -m "社内機能の追加"

# プッシュしてmasterへPR作成
git push -u origin feature/internal-feature
# GitHub上で PR作成: feature/internal-feature → master
```

#### 3. upstreamに貢献する修正を行う場合

upstreamに出す修正の場合：

**重要**: 社内専用の変更（CI/CD設定など）がupstream PRに混入しないよう、以下のcherry-pickワークフローを使用します：

**手順3-1: masterから機能ブランチで開発（社内開発）**

```bash
# 社内開発用にmasterから機能ブランチを作成
git checkout master
git pull origin master
git checkout -b feature/upstream-contribution

# 変更をコミット
git add .
git commit -m "Fix: 修正内容の説明"

# 自分のforkにプッシュしてmasterへPR作成（社内レビュー用）
git push -u origin feature/upstream-contribution
# GitHub上で PR作成: feature/upstream-contribution → master
```

**手順3-2: 社内レビュー後、masterにマージ**

```bash
# PR承認後、GitHub上でマージ
# 機能ブランチの変更がmasterに統合されます
```

**手順3-3: upstream貢献用にcherry-pick（社内変更を除外）**

```bash
# upstream/masterからコントリビュート用ブランチを作成
git checkout upstream/master
git pull upstream master
git checkout -b contrib/upstream-contribution

# upstreamに反映するコミットのみをcherry-pick
# 機能ブランチから対象コミットのハッシュを特定
git log master --oneline -10  # 対象コミットを確認

# 関連するコミットをcherry-pick
git cherry-pick <commit-hash1>
git cherry-pick <commit-hash2>
# または連続する複数コミット: git cherry-pick <start-hash>^..<end-hash>

# コントリビュート用ブランチをプッシュ
git push -u origin contrib/upstream-contribution
```

**手順3-4: upstreamリポジトリへPRを作成**

```bash
# GitHub上で PR作成: nulab/flexmark-java:contrib/upstream-contribution → vsch/flexmark-java:master
# このPRには社内専用の変更を含まず、cherry-pickしたコミットのみが含まれます
```

**手順3-5: upstreamでPRがマージされた後**

⚠️ **GitHub Actionsを使用してupstream/masterを同期してください。手動のgitコマンドは使用しないでください。**

1. GitHubの**Actions**タブを開く
2. **"Sync upstream/master with Upstream Repository"** ワークフローを選択
3. **"Run workflow"** → **"Run workflow"** をクリック
4. ワークフローが自動的にmasterへのPRを作成します
5. 自動作成されたPRを確認・マージ

詳細は[セクション4: upstream同期](#4-upstream同期)のワークフロー手順を参照してください。

#### 4. upstream同期

必要に応じて（例: upstreamがあなたのコントリビュートをマージした後、または最新のupstream変更を取得したい時）`upstream/master`をupstreamリポジトリと同期します。

**⚠️ 重要: `upstream/master`へのpush操作はすべてGitHub Actionsを使用する必要があります。手動でのgit pushは緊急時を除き禁止です。**

**標準方法: GitHub Actionsを使用**

1. GitHubの**Actions**タブを開く
2. ワークフロー一覧から**"Sync upstream/master with Upstream Repository"**を選択
3. **"Run workflow"**ボタンをクリック
4. オプション設定（必要に応じて）:
   - **Use workflow from**: `Branch: upstream/master`のまま
   - **Dry run**: 変更をプレビューのみする場合チェック（任意）
5. **"Run workflow"**をクリックして実行

**自動実行される内容:**
- ✅ upstreamリポジトリ(vsch/flexmark-java)から最新変更を取得
- ✅ `upstream/master`ブランチを更新
- ✅ syncブランチを作成: `sync/upstream-YYYYMMDD-HHMMSS`
- ✅ `master`へのPull Requestを作成:
  - タイトル: "🔄 Sync upstream changes (YYYY-MM-DD)"
  - 詳細なコミット一覧（作者・日時付き）
  - 検証チェックリスト

**ワークフロー完了後:**
1. **Pull Requests**タブを開く
2. 自動作成されたPRを探す（タイトルが🔄で始まる）
3. コミット内容と変更を確認
4. 検証チェックリストを完了
5. 準備ができたらマージ

**Dry Runモード:**
- 変更内容を事前にプレビューしたい場合に使用
- 実行前に"Dry run"オプションをチェック
- ワークフローログで同期される内容を確認
- 確認後、dry runなしで再実行して実際に同期

**緊急時の手動同期のみ**

⚠️ **GitHub Actionsが利用できない場合のみ使用してください。誤った操作はブランチ戦略を破壊する可能性があります。**

```bash
# 警告: 手動同期は慎重な操作が必要です
# upstream/masterを更新
git checkout upstream/master
git pull upstream master
git push origin upstream/master

# syncブランチを手動作成してPR作成
git checkout -b sync/upstream-$(date +%Y%m%d)
git push -u origin sync/upstream-$(date +%Y%m%d)
# GitHub上で手動でPR作成: sync/upstream-YYYYMMDD → master
```

### 重要なルール

1. ✅ **許可される操作**
   - 機能ブランチから`master`へのPR作成
   - `contrib/*`ブランチからupstreamリポジトリへのPR作成（cherry-pickしたコミットを使用）
   - `upstream/master`から`master`へのPR作成
   - upstreamから`upstream/master`への変更のpull

2. ❌ **禁止される操作**
   - `master`からupstreamリポジトリへのPR作成（cherry-pickワークフローを使用すること）
   - `feature/*`ブランチから直接upstreamへのPR作成（cherry-pickした`contrib/*`ブランチを使用すること）
   - **`upstream/master`への手動push（必ずGitHub Actionsワークフローを使用すること）**
   - このリポジトリ内での`upstream/master`を対象とするPR作成・マージ
   - GitHub Actionsワークフロー以外での`upstream/master`の変更

3. 🔑 **重要な戦略**
   - 必ず最初に`master`から`feature/*`ブランチで開発
   - `master`へマージ後、`upstream/master`から`contrib/*`ブランチを作成
   - `git cherry-pick`で関連コミットのみを`contrib/*`ブランチに移す
   - これにより社内専用の変更（CI/CD等）がupstream PRに混入することを防ぐ
   - **すべての`upstream/master`同期にはGitHub Actionsワークフローを使用**

### ブランチ保護設定の推奨

GitHub上で以下のブランチ保護ルールを設定することを推奨します：

**`upstream/master`ブランチ:**
- マージ前にプルリクエストレビューを必須: **OFF**（PRは作成されないため）
- ステータスチェックの合格を必須: **OFF**
- プッシュ可能なユーザーを制限: **ON**（誰も直接プッシュできないように）
- ブランチをロック: **検討する**（誤った変更を防ぐため）

**`master`ブランチ:**
- マージ前にプルリクエストレビューを必須: **ON**
- ステータスチェックの合格を必須: **ON**（CIが設定されている場合）
- 古いプルリクエストの承認を却下: **ON**

---

## Diagram / 図解

```
┌────────────────────────────────────────────────────────────┐
│         Upstream Repository (vsch/flexmark-java)           │
└────────────────────────────────────────────────────────────┘
         ↑                              │
         │                              │ pull (after upstream merge)
        PR                              ↓
         │                      upstream/master ────PR───→ master
         │                                                   ↑
         │                                                   │
    contrib/xxx                                              │
    (cherry-pick)                                            │
         ↑                                                   │
         │                                                   │
    feature/xxx ───────────────────PR───────────────────────→┘
    (develop)                      (internal review)

※contrib/xxx checkout from upstream/master
※feature/xxx checkout from master
```

### Flow Summary / フロー概要

```
Internal Only:
  master → feature/xxx → PR → master

Upstream Contribution (Cherry-pick workflow):
  1. master → feature/xxx → PR → master (internal review & merge)
  2. upstream/master → contrib/xxx → cherry-pick commits → PR → vsch/flexmark-java:master
  3. (after upstream merge) Run GitHub Actions workflow → upstream/master updated → PR to master
  4. Review and merge the auto-created PR
```

---

## Troubleshooting / トラブルシューティング

### Accidentally committed to upstream/master / 誤ってupstream/masterにコミットしてしまった場合

```bash
# Reset to match upstream (WARNING: discards local commits)
git checkout upstream/master
git fetch upstream
git reset --hard upstream/master
git push -f origin upstream/master
```

### Conflict when syncing upstream to master / upstreamからmasterへの同期で競合が発生した場合

```bash
# On sync branch
git checkout sync/upstream-to-master
git merge master
# Resolve conflicts
git add .
git commit -m "Merge master and resolve conflicts"
git push origin sync/upstream-to-master
```

### Lost track of upstream/master / upstream/masterの追跡を見失った場合

```bash
# Re-create upstream/master branch
git fetch upstream
git branch -D upstream/master
git checkout -b upstream/master upstream/master
git push -f origin upstream/master
```

### Cherry-pick conflicts / cherry-pick時の競合

```bash
# If conflict occurs during cherry-pick
git status  # Check conflicting files
# Resolve conflicts manually
git add .
git cherry-pick --continue

# To abort cherry-pick
git cherry-pick --abort

# To skip a problematic commit
git cherry-pick --skip
```

### Wrong commits cherry-picked / 誤ったコミットをcherry-pickしてしまった場合

```bash
# Reset contrib branch and start over
git checkout contrib/upstream-contribution
git reset --hard upstream/master
# Re-do cherry-pick with correct commits
git cherry-pick <correct-commit-hash1>
git cherry-pick <correct-commit-hash2>
git push -f origin contrib/upstream-contribution
```
