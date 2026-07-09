/**
 * user(신원/계정) 모듈은 거의 모든 도메인이 참조하는 공유 커널(shared kernel)이라
 * OPEN 으로 열어 다른 모듈이 User/UserRepository 를 직접 사용할 수 있게 한다.
 * (global 모듈과 동일한 취급)
 *
 * 더 엄격히 가고 싶으면: 다른 모듈이 UserRepository 를 직접 쓰지 않고
 * user 가 노출한 UserService(파사드)만 호출하도록 리팩터링 → 그때 이 OPEN 을 제거.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.canvasflow.user;
