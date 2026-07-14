package com.canvasflow.mypage;

import com.canvasflow.mypage.dto.MyPageResponse;

/**
 * mypage 모듈이 다른 모듈에 노출하는 기능을 모은 파사드 인터페이스.
 * com.canvasflow.mypage는 이 모듈의 기본 패키지라 Spring Modulith가 자동으로 노출해준다.
 * 현재는 MyPageController(같은 모듈)만 쓰지만, 다른 모듈이 나중에 마이페이지 요약 정보가
 * 필요해지면 MyPageService를 직접 참조하지 않고 이 인터페이스로만 의존하도록 미리 준비해둔다.
 * (MyPageResponse는 아직 mypage.dto - internal이라, 실제로 다른 모듈이 이 메서드를 쓰게 되면
 * 그때 MyPageResponse도 노출 위치를 재검토해야 한다.)
 */
public interface MyPageFacade {

    MyPageResponse getSummary(Long userId);
}
