package hello.itemservice.web.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    public static final String LOG_ID = "logId";

    /**
     * preHandle : 컨트롤러 호출 전에 호출됨(더 정확히는 핸들러 어댑터 호출 전에 호출됨)
     * preHandle 의 응답값이 true 이면 다음으로 진행하고
     * false 인 경우 나머지 인터셉터는 물론이고, 핸들러 어댑터도 호출되지 않음.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String uuid = UUID.randomUUID().toString();
        request.setAttribute(LOG_ID, uuid);

        //@RequestMapping: HandlerMethod
        //정적 리소스: ResourceHttpRequestHandler
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler; //호출할 컨트롤러 메서드의 모든 정보가 포함되어 있음
        }

        log.info("REQUEST [{}][{}][{}]", uuid, requestURI, handler);
        return true; // true를 리턴해야 다음 단계 진행
    }

    /**
     * postHandle : 컨트롤러 호출 후에 호출됨. (더 정확히는 핸들러 어댑터 호출 후에 호출됨)
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle [{}]", modelAndView);
    }

    /**
     * afterCompletion : 뷰가 렌더링 된 이후에 호출되며,
     * 핸들러 이하 레벨에서 예외가 발생하여도 항상 호출됨(ex 파라미터를 통해 예외 정보를 받아옴)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        String logId = (String)request.getAttribute(LOG_ID);
        log.info("RESPONSE [{}][{}]", logId, requestURI);

        if (ex != null) { // 에러 출력
            log.error("afterCompletion error!!", ex);
        }
    }
}
