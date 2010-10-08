package edu.brown.catalog;

import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.voltdb.catalog.*;

import edu.brown.utils.ArgumentsParser;
import edu.brown.utils.FileUtil;

public abstract class HStoreConf {
    private static final Logger LOG = Logger.getLogger(HStoreConf.class);
    
    /**
     * Converts a host/site/partition information stored in a catalog object to 
     * a configuration file used by our DTXN system
     * @param catalog
     * @return
     * @throws Exception
     */
    public static String toHStoreConf(Catalog catalog) throws Exception {
        final boolean debug = LOG.isDebugEnabled();
        Map<Host, Set<Site>> host_sites = CatalogUtil.getSitesPerHost(catalog);
        
        TreeMap<Integer, String> sorted_output = new TreeMap<Integer, String>();
        for (Host catalog_host : host_sites.keySet()) {
            if (debug) LOG.debug(catalog_host + ": " + host_sites.get(catalog_host));
            for (Site catalog_site : host_sites.get(catalog_host)) {
                if (debug) LOG.debug("  " + catalog_site + ": " + CatalogUtil.debug(catalog_site.getPartitions()));
                for (Partition catalog_part : catalog_site.getPartitions()) {
                    String hostinfo = catalog_host.getIpaddr() + " " + catalog_site.getPort();
                    sorted_output.put(catalog_part.getId(), hostinfo);    
                }
            } // FOR
        } // FOR
        
        StringBuilder buffer = new StringBuilder();
        String add = "";
        for (Entry<Integer, String> e : sorted_output.entrySet()) {
            buffer.append(add)
                  .append(e.getKey() + "\n")
                  .append(e.getValue());
            add = "\n\n";
        }
        return (buffer.toString());
    }
    
    public static void main(String[] vargs) throws Exception {
        ArgumentsParser args = ArgumentsParser.load(vargs);
        args.require(ArgumentsParser.PARAM_CATALOG, ArgumentsParser.PARAM_SIMULATOR_CONF_OUTPUT);
        
        String contents = HStoreConf.toHStoreConf(args.catalog);
        assert(!contents.isEmpty());
        
        String output_path = args.getParam(ArgumentsParser.PARAM_SIMULATOR_CONF_OUTPUT);
        FileUtil.writeStringToFile(output_path, contents);
    }
}