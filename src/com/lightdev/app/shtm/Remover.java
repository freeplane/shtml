package com.lightdev.app.shtm;

import org.apache.commons.lang.StringUtils;

class Remover {
	private final String processedText;
    private int begin;
    private int end;
	
	public String getProcessedText() {
		return processedText.substring(begin, end);
	}
	public Remover(String text) {
		super();
		this.processedText = text;
		this.begin = 0;
		this.end = text.length();
	}

	public Remover removeFirstAndBefore(String element){
        this.begin = indexOfFirstCharacterAfter('<' + element);
        return this;
	}
	
	private int indexOfFirstCharacterAfter(String element) {
        int index = StringUtils.indexOfIgnoreCase(processedText, element, begin) + 1;
        if(index <= begin)
            return begin;
        index = processedText.indexOf('>', index) + 1;
        if(index <= begin)
            return begin;
        while(index < end && Character.isWhitespace(processedText.charAt(index)))
            index++;
        return index;
    }
    public Remover removeLastAndAfter(String element){
        this.end = indexOfLastCharacterBefore('<' + element);
        return this;
	}
    
    private int indexOfLastCharacterBefore(String element) {
        int index = StringUtils.lastIndexOfIgnoreCase(processedText, element, end) - 1;
        if(index < begin)
            return end;
         while(index > begin && Character.isWhitespace(processedText.charAt(index)))
            index--;
        return index + 1;
    }
    static public void main(String[] argv){
        assert new Remover("<html>\n\t<body> 1 </body>\\n\\t<html>")
        .removeFirstAndBefore("body").removeLastAndAfter("/body")
        .getProcessedText().equals("1");
        assert new Remover("<html>\n\t<body2> 1 </body>\\n\\t<html>")
        .removeFirstAndBefore("body").removeLastAndAfter("/body")
        .getProcessedText().equals("1");
        assert new Remover("<html>\n\t<body>4 1 5</body>\\n\\t<html>")
        .removeFirstAndBefore("body").removeLastAndAfter("/body")
        .getProcessedText().equals("4 1 5");
        System.out.println("Success");
    }
}
