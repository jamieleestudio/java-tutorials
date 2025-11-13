package org.third.li.springframework.beans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("bean alias test")
public class BeanAliasTest {

    @Test
     void annotationBeanAlias(){
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(UserManager.class);
        applicationContext.refresh();
        var user = applicationContext.getBean("engineer", User.class);
        var lixiaofeng = applicationContext.getBean("lixiaofeng",User.class);
        assertThat(user).isSameAs(lixiaofeng);
     }

     @Test
     void annotationName(){
         AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
         applicationContext.register(BizService.class);
         applicationContext.register(TeacherController.class);
         applicationContext.register(UserController.class);
         applicationContext.refresh();
         var user = applicationContext.getBean(TeacherController.class);
         var lixiaofeng = applicationContext.getBean(UserController.class);
         assertThat(user.getBizService()).isSameAs(lixiaofeng.getBizService());
     }

}
