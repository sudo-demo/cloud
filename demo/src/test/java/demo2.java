import com.example.common.util.bean.BeanUtils;
import com.example.common.util.proxy.ProxyUtils;
import com.example.demo.service.DemoService;

public class demo2 {

    public static void main(String[] args) {

        DemoService proxy = ProxyUtils.createProxy(DemoService.class);
        proxy.demo2();
        System.out.println(proxy);
        System.out.println(proxy);
    }
}
