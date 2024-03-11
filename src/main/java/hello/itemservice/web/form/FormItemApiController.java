package hello.itemservice.web.form;

import hello.itemservice.domain.item.form.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API에서 검증이 이루어지는 세 가지 케이스
 * 1. 성공
 * 2. JSON을 객체로 생성하는 것 자체를 실패
 * 3. JSON을 객체로 생성하는 것은 성공했지만, 이후 검증에서 실패
 */
@Slf4j
@RestController
@RequestMapping("/form/api/items")
public class FormItemApiController {

    /**
     * @ModelAttribute 는 필드 단위로 정교하게 바인딩이 적용됨.
     * 특정 필드가 바인딩 되지 않아도 나머지 필드는 정상 바인딩 되고, Validator를 사용한 검증도 적용 가능
     * 반면, @RequestBody 는 HttpMessageConverter 단계에서 JSON 데이터를 객체로 변경하지 못하면
     * 이후 단계 자체가 진행되지 못하고 바로 예외 발생
     */
    @PostMapping
    public Object addItem(@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult) {
        log.info("Api 호출");

        if(bindingResult.hasErrors()) {
            log.info("검증 오류", bindingResult);
            return bindingResult.getAllErrors();
        }

        log.info("성공 로직");
        return form;
    }
}
