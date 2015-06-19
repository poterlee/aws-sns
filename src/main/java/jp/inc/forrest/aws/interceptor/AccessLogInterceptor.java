package jp.inc.forrest.aws.interceptor;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * アクセスログインターセプター（サンプル）
 *
 * @author kuhubgit
 *
 */
public class AccessLogInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(AccessLogInterceptor.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept
	 * .MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {

		Object[] arguments = methodInvocation.getArguments();

		if (arguments != null) {
			Object arg = arguments[0];
			Method method = methodInvocation.getMethod();
			logger.info(method.getDeclaringClass().getSimpleName() + "." + method.getName() + " >>> arg: [" + arg.toString() + "]"); // 簡易すぎる(メソッドの引数オブジェクトがtoStringを実装していることが前提)
		}

		// メソッド実行
		return methodInvocation.proceed();

	}

}
