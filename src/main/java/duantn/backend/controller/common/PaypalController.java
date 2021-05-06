package duantn.backend.controller.common;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import duantn.backend.authentication.CustomException;
import duantn.backend.config.paypal.PaypalPaymentIntent;
import duantn.backend.config.paypal.PaypalPaymentMethod;
import duantn.backend.config.paypal.PaypalService;
import duantn.backend.dao.CustomerRepository;
import duantn.backend.dao.TransactionRepository;
import duantn.backend.helper.Helper;
import duantn.backend.model.dto.output.Message;
import duantn.backend.model.entity.Customer;
import duantn.backend.model.entity.Transaction;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;


@RestController
public class PaypalController {

    @Value("${duantn.rate}")
    private Integer rate;

//    private String email;

    public static final String URL_PAYPAL_SUCCESS = "pay/success";
    public static final String URL_PAYPAL_CANCEL = "pay/cancel";
//    private double value;
//    private String description;
//    private Customer customer;

    private Logger log = LoggerFactory.getLogger(getClass());

    final
    Helper helper;

    final
    CustomerRepository customerRepository;

    final
    TransactionRepository transactionRepository;

    private final PaypalService paypalService;

    public PaypalController(PaypalService paypalService, Helper helper, CustomerRepository customerRepository, TransactionRepository transactionRepository) {
        this.paypalService = paypalService;
        this.helper = helper;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/customer/pay")
    public Message pay(HttpServletRequest request, //HttpServletResponse response,
                       @RequestParam("price") double price)
            throws CustomException {
        String cancelUrl = helper.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
        String successUrl = helper.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
        HttpSession session=request.getSession();
        try {
            Double value = Math.round(price * 100.0) / 100.0;
            String email= (String) request.getAttribute("email");
            String description = "Top up your account: " + value + " USD";

            successUrl+="?email1="+email+"&value1="+value;

            if (email == null || email.trim().equals("")) throw new CustomException("Token không hợp lệ");

            Customer customer = customerRepository.findByEmail(email);
            if (customer == null)
                throw new CustomException("Người dùng không hợp lệ");

            Payment payment = paypalService.createPayment(
                    value,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    description,
                    cancelUrl,
                    successUrl);
            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    //response.sendRedirect(links.getHref());
                    return new Message(links.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new CustomException("Thanh toán thất bại");
        }
        //return "redirect:/";
        throw new CustomException("Không xác định");
    }

    @GetMapping(URL_PAYPAL_CANCEL)
    public Message cancelPay() {
        return new Message("Thanh toán bị hủy bỏ");
    }

    @GetMapping(URL_PAYPAL_SUCCESS)
    public void successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId,
                           @RequestParam("email1") String email,
                           @RequestParam("value1") Double value,
                           HttpServletResponse response)
            throws CustomException {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                Customer customer= customerRepository.findByEmail(email);
                String description="Nạp vào tài khoản: "+value+" USD";
                int increase = (int) (value * rate);
                customer.setAccountBalance(customer.getAccountBalance() + increase);
                customerRepository.save(customer);
                creatTransaction(customer, increase, description);
                response.sendRedirect("http://localhost:4200/confirm-payment?usd="+value+"&vnd="+increase);
                //return new Message("Nạp thành công, số tiền: " + value + " USD - tức " + increase + " VNĐ");
                return;
            }
        } catch (PayPalRESTException | IOException e) {
            log.error(e.getMessage());
            throw new CustomException("Có lỗi xảy ra");
        }
        //return "redirect:/";
        throw new CustomException("Không xác định");
    }

    private void creatTransaction(Customer customer, Integer money, String description) {
        Transaction transaction = new Transaction();
        transaction.setAmount(money);
        transaction.setType(true);
        transaction.setDescription(description);
        transaction.setTimeCreated(new Date());
        transaction.setCustomer(customer);
        transactionRepository.save(transaction);
    }
}
