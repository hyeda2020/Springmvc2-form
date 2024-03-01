package hello.itemservice.web.form;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component // 스프링 빈으로 등록(싱글톤으로 관리)
public class ItemValidator implements Validator {

    /**
     * 만약 WebDataBinder에 여러 검증기가 등록된 경우
     * 어떤 검증기가 수행되어야 할지 구분하기 위한 로직을
     * supports 메서드에서 실행
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    // Errors는 BindingResult 클래스의 부모
    public void validate(Object target, Errors errors) {

        Item item = (Item)target;

        /**
         * RejectValue, Reject 메서드 파라미터
         * field : 오류 필드명
         * errorCode : 오류 코드(이 오류 코드는 메시지에 등록된 코드가 아니다. 뒤에서 설명할
         * messageResolver를 위한 오류 코드이다.)
         * errorArgs : 오류 메시지에서 {0} 을 치환하기 위한 값
         * defaultMessage : 오류 메시지를 찾을 수 없을 때 사용하는 기본 메시지
         */

        if (!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.rejectValue("price", "range", new Object[] {1000, 1000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        //특정 필드 예외가 아닌 전체 예외
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }
    }
}
