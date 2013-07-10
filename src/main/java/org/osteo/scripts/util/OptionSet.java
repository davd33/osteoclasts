/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.scripts.util;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author davidr
 */
public class OptionSet extends LinkedList<Option> {
    
    public static final long serialVersionUID = 42L;

    public OptionSet() {
        super();
    }

    public OptionSet(Option o) {
        super();
        this.add(o);
    }

    public void concat(OptionSet os) {
        for (Option o : os) {
            this.add(o);
        }
    }

    public String getOptionValue(String optionName) {
        for (Option o : this) {
            if (o.getName().equals(optionName)) {
                return o.getCurrentValue();
            }
        }
        return null;
    }

    public Option getOption(String optionName) {
        for (Option o : this) {
            if (o.getName().equals(optionName)) {
                return o;
            }
        }
        return null;
    }

    public static Integer nOptionSets(Option[] options, Double[] min, Double[] max, Integer[] nsteps) {
        LinkedList<Object[]> valuesList = new LinkedList<Object[]>();
        LinkedList<Option> optionLinkedList = new LinkedList<Option>();

        genValues(options, valuesList, optionLinkedList, min, max, nsteps);
        int nSets = 1;
        for (Object[] values : valuesList) {
            nSets *= values.length;
        }

        return nSets;
    }

    public static List<OptionSet> genOptSpace(Option[] options, Double[] min, Double[] max, Integer[] nsteps) {
        if (options.length == min.length && options.length == max.length) {
            LinkedList<Object[]> valuesList = new LinkedList<Object[]>();
            LinkedList<Option> optionLinkedList = new LinkedList<Option>();

            genValues(options, valuesList, optionLinkedList, min, max, nsteps);
            List<OptionSet> oss = buildSets(valuesList.listIterator(), optionLinkedList.listIterator());

            return oss;
        } else {
            throw new IllegalArgumentException("there should be as much min and max values as options");
        }
    }

    private static void genValues(Option[] options, LinkedList<Object[]> valuesList, LinkedList<Option> optionLinkedList, Double[] min, Double[] max, Integer[] nsteps) {
        for (int o = 0; o < options.length; o++) {
            Object[] values;
            Option opt = options[o];
            optionLinkedList.add(opt);

            Double Min = min[o];
            Double Max = max[o];
            Integer nstep = nsteps[o];
            if (Min == null || Max == null) { // preconfigured value
                values = opt.getPossibleValues().toArray();
            } else { // values will be generated from min to max, each step
                Double step = 0d;
                if (nstep != 0) {
                    step = (max[o] - min[o]) / nstep;
                }

                int nValues = nstep + 1;
                values = new Double[nValues];
                for (int i = 0; i < nValues; i++) {
                    values[i] = Min + (i * step);
                }
            }

            valuesList.add(values);
        }
    }

    private static LinkedList<OptionSet> buildSets(ListIterator<Object[]> paramsIt, ListIterator<Option> optionsIt) {
        if (paramsIt.hasNext()) {
            Object[] param = paramsIt.next();
            Option option = optionsIt.next();

            LinkedList<OptionSet> oss = new LinkedList<OptionSet>();
            for (Object val : param) {
                LinkedList<OptionSet> recursivOptionSet = buildSets(paramsIt, optionsIt);

                if (recursivOptionSet != null) {
                    for (OptionSet rOs : recursivOptionSet) {
//                        if (!val.toString().equals("NaN")) {
                        OptionSet os = new OptionSet();
                        os.add(new Option(option.getName(), Option.Type.STRING, val.toString()));
                        os.concat(rOs);
                        oss.add(os);
//                        }
                    }
                } else {
//                    if (!val.toString().equals("NaN")) {
                    OptionSet os = new OptionSet();
                    os.add(new Option(option.getName(), Option.Type.STRING, val.toString()));
                    oss.add(os);
//                    }
                }
            }

            optionsIt.previous();
            paramsIt.previous();
            return oss;
        } else {
            return null;
        }
    }

    public static Option findOption(String name, OptionSet currentOptions) {
        if (currentOptions == null) {
            return null;
        }
        for (Option opt : currentOptions) {
            if (opt.getName().equals(name)) {
                return opt;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String str = "";

        str += "{\n";
        for (Option o : this) {
            str += "\t" + o.toString() + ",\n";
        }
        str += "}";

        return str;
    }
}
