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

**Step 3-1: Create feature branch and PR to master (internal review)**

```bash
# Create feature branch from upstream/master
git checkout upstream/master
git pull upstream master
git checkout -b feature/upstream-contribution

# Make changes and commit
git add .
git commit -m "Fix: description of the fix"

# Push to your fork
git push -u origin feature/upstream-contribution
# Create PR: feature/upstream-contribution → master on GitHub (for internal review)
```

**Step 3-2: Create PR to upstream repository (if contribution needed)**

```bash
# After internal review approval, create PR to upstream
# On GitHub, create PR: nulab/flexmark-java:feature/upstream-contribution → vsch/flexmark-java:master
```

**Step 3-3: After upstream PR is merged**

```bash
# Update upstream/master from upstream repository
git checkout upstream/master
git pull upstream master

# Verify the merge
git log --oneline -5

# Push updated upstream/master to origin
git push origin upstream/master
```

**Step 3-4: Sync changes to master**

```bash
# Create sync branch from updated upstream/master
git checkout upstream/master
git checkout -b sync/upstream-to-master

# Push and create PR to master
git push -u origin sync/upstream-to-master
# Then create PR: sync/upstream-to-master → master on GitHub
```

#### 4. Regular Upstream Synchronization

Keep `upstream/master` up-to-date with upstream repository:

```bash
# Update upstream/master
git checkout upstream/master
git pull upstream master
git push origin upstream/master

# If there are updates, sync to master
git checkout -b sync/upstream-$(date +%Y%m%d)
git push -u origin sync/upstream-$(date +%Y%m%d)
# Create PR: sync/upstream-YYYYMMDD → master on GitHub
```

### Important Rules

1. ✅ **Permitted Operations**
   - Create PRs from feature branches to `master`
   - Create PRs directly to upstream repository
   - Create PRs from `upstream/master` to `master`
   - Pull changes from upstream to `upstream/master`

2. ❌ **PROHIBITED Operations**
   - Creating PRs from `master` to upstream repository
   - Direct pushes to `upstream/master`
   - Creating/merging PRs targeting `upstream/master` within this repository
   - Any modifications to `upstream/master` except pulling from upstream

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

**手順3-1: 機能ブランチを作成してmasterへPR（社内レビュー）**

```bash
# upstream/masterから機能ブランチを作成
git checkout upstream/master
git pull upstream master
git checkout -b feature/upstream-contribution

# 変更をコミット
git add .
git commit -m "Fix: 修正内容の説明"

# 自分のforkにプッシュ
git push -u origin feature/upstream-contribution
# GitHub上で PR作成: feature/upstream-contribution → master（社内レビュー用）
```

**手順3-2: upstreamリポジトリへPRを作成（貢献が必要な場合）**

```bash
# 社内レビュー承認後、upstreamへのPRを作成
# GitHub上で PR作成: nulab/flexmark-java:feature/upstream-contribution → vsch/flexmark-java:master
```

**手順3-3: upstreamでPRがマージされた後**

```bash
# upstream/masterをupstreamリポジトリから更新
git checkout upstream/master
git pull upstream master

# マージされたことを確認
git log --oneline -5

# 更新したupstream/masterをoriginにプッシュ
git push origin upstream/master
```

**手順3-4: 変更をmasterに同期**

```bash
# 更新されたupstream/masterから同期ブランチを作成
git checkout upstream/master
git checkout -b sync/upstream-to-master

# プッシュしてmasterへPR作成
git push -u origin sync/upstream-to-master
# GitHub上で PR作成: sync/upstream-to-master → master
```

#### 4. 定期的なupstream同期

`upstream/master`をupstreamリポジトリと同期：

```bash
# upstream/masterを更新
git checkout upstream/master
git pull upstream master
git push origin upstream/master

# 更新がある場合、masterに同期
git checkout -b sync/upstream-$(date +%Y%m%d)
git push -u origin sync/upstream-$(date +%Y%m%d)
# GitHub上で PR作成: sync/upstream-YYYYMMDD → master
```

### 重要なルール

1. ✅ **許可される操作**
   - 機能ブランチから`master`へのPR作成
   - upstreamリポジトリへの直接PR作成
   - `upstream/master`から`master`へのPR作成
   - upstreamから`upstream/master`への変更のpull

2. ❌ **禁止される操作**
   - `master`からupstreamリポジトリへのPR作成
   - `upstream/master`への直接プッシュ
   - このリポジトリ内での`upstream/master`を対象とするPR作成・マージ
   - upstreamからのpull以外での`upstream/master`の変更

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
        PR(when contributing)           ↓
         |                      upstream/master ────PR───→ master
         │                                                    ↑
         │                                                    │
         │                                                    |
    feature/xxx ───────PR─────────────────────────────────────┘
```

### Flow Summary / フロー概要

```
Internal Only:
  master → feature/xxx → PR → master

Upstream Contribution:
  1. upstream/master → feature/xxx → PR → master (internal review)
  2. feature/xxx → PR → vsch/flexmark-java:master (if contribution needed)
  3. (after upstream merge) vsch/flexmark-java:master → pull → upstream/master
  4. upstream/master → sync/xxx → PR → master
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
