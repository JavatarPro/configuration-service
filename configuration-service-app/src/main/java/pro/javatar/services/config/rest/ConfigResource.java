package pro.javatar.services.config.rest;

import pro.javatar.services.config.helper.ConfigHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class ConfigResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigResource.class);

    @Autowired
    private ConfigHelper configHelper;

    @RequestMapping(method = RequestMethod.POST, value = "webhook")
    public void webhook(@RequestBody String payload) {
        LOG.info("Webhook request has been received");
        Set<String> servicesList = configHelper.retrieveModifiedServices(payload);
        for (String service : servicesList) {
            configHelper.refreshService(service);
            LOG.info("Following service: {} was refreshed", service);
        }
    }
}
