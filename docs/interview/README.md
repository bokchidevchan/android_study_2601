# 기술 면접 준비 자료

> 면접에서 자주 나오는 Android Testing 관련 질문 정리

---

## 문서 목록

### 1. [면접 심화 가이드](./INTERVIEW_DEEP_DIVE.md)

**"왜?"에 답할 수 있는 깊이 있는 내용**

- Main Dispatcher 교체 원리
- Virtual Time 동작 방식
- 테스트 더블 선택 기준
- Turbine, Compose Semantics Tree 등

### 2. [면접 치트시트](./INTERVIEW_CHEATSHEET.md)

**면접 직전 빠르게 훑어보는 핵심 정리**

- 주요 개념 한 줄 정리
- 코드 예제
- 자주 하는 실수
- 예상 Q&A

---

## 핵심 면접 질문 미리보기

| 질문 | 핵심 답변 |
|------|----------|
| ViewModel 테스트 시 Main Dispatcher를 왜 교체? | JVM에 Android Main Looper가 없어서 |
| Mock과 Stub의 차이는? | Mock=행위검증, Stub=상태검증 |
| runTest에서 delay가 빠른 이유? | Virtual Time 사용 |
| Turbine을 왜 사용? | Flow의 상태 변화 순서를 검증하기 위해 |
| sealed class로 상태 정의하는 이유? | 컴파일 타임 안전, when에서 모든 케이스 강제 |
| coEvery vs every 차이? | suspend 함수는 coEvery 사용 |

---

## 추천 학습 순서

```
1. INTERVIEW_CHEATSHEET.md → 전체 훑어보기 (15분)
2. INTERVIEW_DEEP_DIVE.md  → 깊이 이해하기 (1시간)
3. 소리내어 답변 연습      → 말하기 훈련 (30분)
```

---

*면접 파이팅!*
