package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtTable extends SwtUiComponent implements Table {

    private SWTBotTable target;

    public SwtTable(SWTBotTable target) {
        super(target);
        this.target = target;
    }

    @Override
    public List<String> getColumnNames() {
        return target.columns();
    }

    @Override
    public String getCell(int row, String columnName) {
        return target.cell(row, columnName);
    }

    @Override
    public int getRowIndex(String cellValue, String columnName) {
        return target.indexOf(cellValue, columnName);
    }

    @Override
    public int getRowCount() {
        return target.rowCount();
    }

    @Override
    public void select(int row) {
        target.select(row);
    }

    @Override
    public void deselect() {
        target.unselect();
    }

    @Override
    public int getSelectedRowCount() {
        return target.selection().rowCount();
    }

}
