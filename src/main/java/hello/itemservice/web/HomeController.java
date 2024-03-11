package hello.itemservice.web;

import hello.itemservice.domain.member.Member;
import hello.itemservice.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;

    /**
     * 로그인 하지 않은 사용자도 홈에 접근할 수 있기 때문에 `
     * @CookieValue 에서 required = false` 를 사용
     */
    @GetMapping("/")
    public String LoginHome(
            @CookieValue(name = "memberId", required = false) Long memberId,
            Model model) {

        if (memberId == null) {
            log.info("memberId == null");
            return "home";
        }

        Member member = memberRepository.findById(memberId);
        if (member == null) {
            log.info("memberOptional.isEmpty");
            return "home";
        }

        log.info("loginMember={}", member);
        model.addAttribute("member", member);
        return "loginHome";
    }
}
