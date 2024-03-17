package hello.itemservice.web;

import hello.itemservice.domain.member.Member;
import hello.itemservice.domain.member.MemberRepository;
import hello.itemservice.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final SessionManager sessionManager;

    /**
     * 스프링에서 제공하는 세션 관리 기능 적용
     * (@SessionAttribute)
     */
    @GetMapping("/")
    public String LoginHome(
            @SessionAttribute(name = "loginMember", required = false) Member member, Model model) {

        // 세션 관리자에 저장된 회원 정보 조회
//        Member member = (Member)sessionManager.getSession(request);

        /**
         * request.getSession() 를 사용하면 기본 값이 create 파라미터 옵션 값이 true 이므로,
         * 로그인 하지 않을 사용자도 의미없는 세션이 만들어짐.
         * 따라서 세션을 찾아서 사용하는 시점에는 false 옵션을 사용해서 세션을 생성하지 않아야 함.
         */
//        HttpSession session = request.getSession(false);
//        Member member = (Member)session.getAttribute(SessionConst.LOGIN_MEMBER.getValue());

        if (member == null) {
            log.info("memberOptional.isEmpty");
            return "home";
        }

        log.info("loginMember={}", member);
        model.addAttribute("member", member);
        return "loginHome";
    }
}
