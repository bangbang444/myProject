프로젝트의 기존 PR 템플릿을 사용하여 GitHub Pull Request를 생성합니다.

`.github/PULL_REQUEST_TEMPLATE.md`의 템플릿을 읽고 PR 본문으로 사용하세요.

사용자가 제공할 정보:
- PR 제목
- 관련 이슈 번호 (Closed #번호)
- PR Point (리뷰어가 집중해서 볼 내용, 변경 사항 및 이유)
- 참고 사항 (선택)

절차:
1. $ARGUMENTS에서 정보를 파싱합니다.
2. 부족한 정보가 있으면 사용자에게 질문합니다.
3. 현재 브랜치의 변경 사항(`git log`, `git diff develop...HEAD`)을 확인하여 PR Point 작성에 참고합니다.
4. 템플릿에 정보를 채워 넣습니다.
5. base 브랜치는 `develop`으로 설정합니다.
6. PR을 올린다. (gh 사용 금지)
