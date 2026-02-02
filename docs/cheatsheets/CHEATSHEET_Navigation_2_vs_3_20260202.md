# Navigation 2 vs 3 치트시트

> 최근 대화 기반 핵심 정리 | 2026-02-02

---

## 1. 핵심 개념

| 개념 | 설명 | 면접 포인트 |
|------|------|------------|
| NavController | Nav2의 상태 관리자, 화면 전환 명령 | 내부에서 백스택 관리, 직접 조작 제한 |
| NavHost | Nav2의 UI 컨테이너, 네비게이션 그래프 정의 | Controller와 분리된 역할 설명 필요 |
| NavBackStack | Nav3의 백스택, 직접 조작 가능 | "You own the back stack" 철학 |
| NavDisplay | Nav3의 UI 컨테이너, Scene 지원 | NavHost 대체, 레이아웃 전략 지원 |
| NavKey | Nav3의 타입 안전 라우트 인터페이스 | @Serializable과 함께 사용 |
| Scene | Nav3의 레이아웃 전략 | Single, Dialog, TwoPane 지원 |

---

## 2. API 비교표

| 구분 | Navigation 2 | Navigation 3 |
|------|--------------|--------------|
| 상태 관리 | `NavController` | `NavBackStack` |
| UI 컨테이너 | `NavHost` | `NavDisplay` |
| 목적지 정의 | `composable<Route> { }` | `entry<Route> { }` |
| 라우트 타입 | `data class` (Serializable) | `NavKey` (Serializable) |
| 화면 이동 | `navController.navigate()` | `backStack.add()` |
| 뒤로 가기 | `navController.popBackStack()` | `backStack.removeLastOrNull()` |
| 인자 접근 | `backStackEntry.toRoute()` | entry 람다의 `key` 파라미터 |
| 레이아웃 전략 | 제한적 | Scene (Dialog, TwoPane) |
| 안정성 | Stable | Alpha/Experimental |

---

## 3. 코드 스니펫

### Navigation 2 기본 패턴
```kotlin
// Route 정의
@Serializable
data class ProductDetail(val productId: String)

// NavHost 설정
val navController = rememberNavController()
NavHost(navController, startDestination = Home) {
    composable<Home> { 
        HomeScreen(
            onClick = { navController.navigate(ProductDetail("123")) }
        )
    }
    composable<ProductDetail> { entry ->
        val product: ProductDetail = entry.toRoute()
        DetailScreen(product.productId)
    }
}

// 뒤로가기
navController.popBackStack()
```

### Navigation 3 기본 패턴
```kotlin
// Route 정의 (NavKey 구현 필수)
@Serializable
data class ProductDetail(val productId: String) : NavKey

// NavDisplay 설정
val backStack = rememberNavBackStack(Home)
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider {
        entry<Home> { 
            HomeScreen(
                onClick = { backStack.add(ProductDetail("123")) }
            )
        }
        entry<ProductDetail> { key ->
            // key가 직접 ProductDetail 타입 (toRoute 불필요)
            DetailScreen(key.productId)
        }
    }
)

// 뒤로가기
backStack.removeLastOrNull()
```

### Bottom Navigation (Nav 2)
```kotlin
navController.navigate(screen.route) {
    popUpTo(navController.graph.startDestinationId) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

### Scene 활용 (Nav 3)
```kotlin
// Dialog Scene
entry<ConfirmDialog>(
    metadata = DialogSceneStrategy.dialog()
) { key ->
    AlertDialog(
        onDismissRequest = { backStack.removeLastOrNull() },
        title = { Text("확인") },
        text = { Text(key.message) }
    )
}

// Two-Pane Scene (태블릿)
val twoPaneStrategy = rememberTwoPaneSceneStrategy<NavKey>()
NavDisplay(
    backStack = backStack,
    sceneStrategy = twoPaneStrategy,
    // ...
)
```

---

## 4. 선택 가이드

| Navigation 2 선택 | Navigation 3 선택 |
|------------------|------------------|
| 기존 프로젝트 유지보수 | 새 프로젝트 시작 |
| 프로덕션 안정성 중시 | 태블릿/폴더블 지원 필수 |
| 팀원 학습 곡선 고려 | 복잡한 백스택 조작 필요 |
| 풍부한 레퍼런스 필요 | 최신 API 실험 가능 |
| 단순한 네비게이션 구조 | Two-pane 레이아웃 필요 |

---

## 5. 마이그레이션 체크리스트

| 단계 | Before (Nav 2) | After (Nav 3) |
|------|----------------|---------------|
| Route 정의 | `data class Product(val id: String)` | `data class Product(val id: String) : NavKey` |
| 컨트롤러 | `rememberNavController()` | `rememberNavBackStack(StartRoute)` |
| 컨테이너 | `NavHost(...)` | `NavDisplay(...)` |
| 목적지 | `composable<Route> { }` | `entry<Route> { }` |
| 화면 이동 | `navController.navigate(route)` | `backStack.add(route)` |
| 뒤로 가기 | `navController.popBackStack()` | `backStack.removeLastOrNull()` |
| 인자 추출 | `entry.toRoute()` | 람다 파라미터 `key` 직접 사용 |

---

## 6. 면접 Q&A

| 질문 | 핵심 답변 |
|------|----------|
| Nav 2와 Nav 3의 핵심 차이? | Nav2는 NavController가 내부 상태 관리, Nav3는 "You own the back stack" 철학으로 직접 백스택 조작 |
| NavController vs NavBackStack? | NavController는 캡슐화된 상태 관리, NavBackStack은 리스트처럼 add/remove로 직접 조작 |
| Scene이란? | Nav3의 레이아웃 전략. Single(기본), Dialog(팝업), TwoPane(태블릿) 등 지원 |
| NavKey 역할? | Nav3의 타입 안전 라우트 인터페이스. @Serializable과 함께 사용하여 컴파일 타임 검증 |
| 언제 Nav 3 사용? | Two-pane 필요, 복잡한 백스택 조작, 새 프로젝트. 기존 앱은 안정성 위해 Nav 2 유지 권장 |
| Type-Safe Route 왜 중요? | String route는 오타/런타임 에러 위험, data class로 컴파일 타임 검증 및 IDE 자동완성 |
| Nav 2에서 탭 전환 시 상태 유지? | `saveState = true`, `restoreState = true`, `launchSingleTop = true` 조합 |
| Nav 3의 단점? | 아직 Alpha, Breaking changes 가능, 문서/커뮤니티 부족, Hilt 통합 미성숙 |

---

## 7. 자주 하는 실수

| 실수 | 해결 |
|------|------|
| String route 사용 | `@Serializable data class`로 타입 안전 라우트 사용 |
| popBackStack 대신 navigate로 뒤로가기 | `popBackStack()`은 백스택에서 제거, `navigate()`는 새 화면 추가 |
| Bottom Nav 탭 전환 시 상태 초기화 | `saveState`, `restoreState`, `launchSingleTop` 옵션 설정 |
| Nav 3에서 NavKey 미구현 | `@Serializable data class Route(...) : NavKey` 필수 |
| toRoute() 호출 잊음 (Nav 2) | `backStackEntry.toRoute<Route>()`로 인자 추출 |
| Nav 3 프로덕션 적용 | 아직 Alpha 단계, 기존 앱은 Nav 2 유지 권장 |

---

## 8. 핵심 한 줄 요약

```
Navigation 2: NavController가 백스택을 "관리"한다
Navigation 3: 개발자가 백스택을 "소유"한다 (You own the back stack)
```

---

*Generated from conversation on 2026-02-02*
