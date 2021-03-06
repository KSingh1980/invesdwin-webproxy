package de.invesdwin.webproxy.internal;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import com.gargoylesoftware.htmlunit.WebClient;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.GetStringConfig;
import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.WebClientFactory;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.internal.proxypool.BrokerProxyPoolableObjectFactory;
import de.invesdwin.webproxy.internal.proxypool.PooledProxy;

@Named
@ThreadSafe
public class WebproxyServiceHelper {

    @Inject
    private ProxyVerification proxyVeri;
    @Inject
    private BrokerProxyPoolableObjectFactory brokerProxyPoolableObjectFactory;

    public Proxy newProxy(final GetStringConfig config) throws InterruptedException {
        while (true) {
            final PooledProxy proxy = brokerProxyPoolableObjectFactory.makeObject();
            if (brokerProxyPoolableObjectFactory.validateObject(proxy)
                    && proxyVeri.isOfMinProxyQuality(proxy, config.getMinProxyQuality())) {
                return proxy;
            } else {
                continue;
            }
        }
    }

    public int getLastWorkingProxyCount() {
        return brokerProxyPoolableObjectFactory.getLastWorkingProxyCount();
    }

    public WebClient newWebClient(final GetPageConfig config) {
        Assertions.assertThat(config.isUseProxyPool())
        .as("newWebClient() can only be called with fixed proxies. For proxy pooling you have to use getPage() instead!")
        .isFalse();
        return WebClientFactory.initWebClient(config, config.getFixedProxy());
    }

}
