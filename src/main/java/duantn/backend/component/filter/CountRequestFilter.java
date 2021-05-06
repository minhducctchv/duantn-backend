package duantn.backend.component.filter;

import duantn.backend.dao.CountRequestRepository;
import duantn.backend.helper.Helper;
import duantn.backend.model.entity.CountRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with YourComputer.
 * User: DUC_PRO
 * Date: 28/04/2021
 * Time: 1:33 SA
 */

@Component
public class CountRequestFilter implements Filter {
    final
    CountRequestRepository countRequestRepository;

    public CountRequestFilter(CountRequestRepository countRequestRepository) {
        this.countRequestRepository = countRequestRepository;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        String url = ((HttpServletRequest) request).getRequestURI();
        if (!url.contains("/admin") && !url.contains("/super-admin")) {
            CountRequest countRequest = countRequestRepository.findFirstBy(Sort.by("date").descending());
            countRequest.setCount(countRequest.getCount() + 1);
            countRequestRepository.save(countRequest);
        }
        chain.doFilter(request, response);
    }
}
