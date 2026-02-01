# Android Study 문서 센터

> Android 테스팅, 아키텍처, 면접 준비를 위한 종합 가이드

---

## 문서 구조

```
docs/
├── interview/       # 기술 면접 준비 자료
├── cheatsheet/      # 빠른 참조용 치트시트
└── guides/          # 심화 학습 가이드
```

---

## 면접 준비 자료

| 문서 | 설명 | 바로가기 |
|------|------|---------|
| **면접 심화 가이드** | "왜?"에 답할 수 있는 깊이 있는 내용 | [INTERVIEW_DEEP_DIVE.md](./interview/INTERVIEW_DEEP_DIVE.md) |
| **면접 치트시트** | 면접 전 빠르게 훑어보는 핵심 정리 | [INTERVIEW_CHEATSHEET.md](./interview/INTERVIEW_CHEATSHEET.md) |

---

## 치트시트

| 문서 | 설명 | 바로가기 |
|------|------|---------|
| **Testing 치트시트** | JUnit, MockK, Coroutine, Compose 빠른 참조 | [TESTING_CHEATSHEET.md](./cheatsheet/TESTING_CHEATSHEET.md) |

> `/cheatsheet` 명령으로 생성된 치트시트도 이 폴더에 저장됩니다.

---

## 학습 가이드

| 문서 | 설명 | 바로가기 |
|------|------|---------|
| **Testing 완벽 가이드** | 테스트 피라미드부터 실전까지 종합 가이드 | [TESTING_GUIDE.md](./guides/TESTING_GUIDE.md) |
| **Hilt 테스팅 가이드** | Unit Test vs Integration Test에서 Hilt 역할 | [HILT_TESTING_GUIDE.md](./guides/HILT_TESTING_GUIDE.md) |

---

## 추천 학습 순서

### 처음 시작하는 경우

```
1. guides/TESTING_GUIDE.md     → 전체 그림 파악
2. cheatsheet/TESTING_CHEATSHEET.md → 핵심 문법 정리
3. 실제 테스트 코드 작성 실습
```

### 면접 준비 시

```
1. interview/INTERVIEW_CHEATSHEET.md → 빠른 복습
2. interview/INTERVIEW_DEEP_DIVE.md  → "왜?" 질문 대비
3. 예상 질문 소리내어 답변 연습
```

---

## 관련 코드 위치

실제 테스트 코드는 아래 경로에서 확인하세요:

```
app/src/
├── test/           # Unit Tests (JVM)
└── androidTest/    # Instrumented Tests (기기/에뮬레이터)
```

---

*문서 업데이트: 2025년 2월*
