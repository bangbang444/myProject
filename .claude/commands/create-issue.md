프로젝트의 기존 이슈 템플릿을 사용하여 GitHub 이슈를 생성합니다.

`.github/ISSUE_TEMPLATE/be-issue-template.md`의 템플릿을 읽고 이슈 본문으로 사용하세요.

사용자가 제공할 정보:
- 이슈 제목 ([FEAT], [FIX], [REFACTOR] 등 타입 접두사 포함)
- 기능 설명 (📌 Feature Issue 섹션)
- 할 일 목록 (📝 To-do 섹션)

절차:
1. $ARGUMENTS에서 제목, 설명, 할 일 목록을 파싱합니다.
2. 부족한 정보가 있으면 사용자에게 질문합니다.
3. 템플릿에 정보를 채워 넣습니다.
4. Github에 올린다. (gh 사용 금지)
