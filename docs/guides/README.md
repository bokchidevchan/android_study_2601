# 학습 가이드 모음

> 심화 학습을 위한 종합 가이드

---

## 문서 목록

### 1. [Testing 완벽 가이드](./TESTING_GUIDE.md)

**테스트 피라미드부터 실전까지**

- 테스트 피라미드 개념
- 프로젝트 구조
- 단계별 학습 순서 (JUnit → MockK → Coroutine → Compose → TDD)
- 핵심 패턴 정리
- 실행 방법
- 치트시트

### 2. [Hilt 테스팅 가이드](./HILT_TESTING_GUIDE.md)

**Unit Test vs Integration Test에서 Hilt의 역할**

- "그냥 생성자에 Fake 넣으면 되잖아?" 에 대한 답
- Unit Test vs Integration Test 차이
- Hilt 어노테이션 가이드 (@HiltAndroidTest, @BindValue 등)

---

## 학습 로드맵

```
초급
├── TESTING_GUIDE.md의 Step 1-2 (JUnit, MockK)
│
중급
├── TESTING_GUIDE.md의 Step 3-4 (Coroutine, Compose)
├── HILT_TESTING_GUIDE.md
│
고급
├── TESTING_GUIDE.md의 Step 5 (TDD)
└── 실제 프로젝트에 테스트 적용
```

---

## 추가 학습 자료

### 공식 문서
- [Android Testing 공식 문서](https://developer.android.com/training/testing)
- [MockK 공식 문서](https://mockk.io/)
- [Turbine GitHub](https://github.com/cashapp/turbine)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)

### 관련 코드
```
app/src/test/          # Unit Test 예제
app/src/androidTest/   # Instrumented Test 예제
```

---

*이 가이드와 함께 각 테스트 파일의 주석을 읽으며 학습하세요!*
