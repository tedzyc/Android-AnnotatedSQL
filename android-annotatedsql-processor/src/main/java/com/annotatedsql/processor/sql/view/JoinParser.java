package com.annotatedsql.processor.sql.view;

import com.annotatedsql.ParserEnv;
import com.annotatedsql.annotation.sql.Join;
import com.annotatedsql.ftl.ColumnMeta;
import com.annotatedsql.processor.sql.SimpleViewParser;
import com.annotatedsql.util.Where;

import java.util.List;

import javax.lang.model.element.Element;

public class JoinParser extends ExcludeStaticWhereViewParser<FromResult, Join> {

    public JoinParser(ParserEnv parserEnv, SimpleViewParser parentParser, Element f) {
        super(parserEnv, parentParser, f, false);
    }

    @Override
    public FromResult parse() {
        checkColumnExists(annotation.joinColumn());

        StringBuilder sql = new StringBuilder();
        switch (annotation.type()) {
            case INNER:
                sql.append(" JOIN ");
                break;
            case LEFT:
                sql.append(" LEFT OUTER JOIN ");
                break;
            case RIGHT:
                sql.append(" RIGHT OUTER JOIN ");
                break;
            case CROSS:
                sql.append(" CROSS JOIN ");
                break;
        }

        sql.append(annotation.joinTable()).append(" AS ").append(aliasName)
                .append(" ON ").append(aliasName).append('.').append(annotation.joinColumn())
                .append(" = ").append(annotation.onTableAlias()).append('.').append(annotation.onColumn());

        Where where = parserEnv.getTableWhere(tableName);

        String excludeWhere = getExcludeStaticWhere();
        if (excludeWhere != null && where != null) {
            where = where.exclude(excludeWhere);
        }
        if (where != null && !where.isEmpty()) {
            sql.append(" and ").append(where.copy(aliasName).getAsCondition());
        }
        List<ColumnMeta> columns = parseColumns();
        return new FromResult(aliasName, sql.toString(), toSqlSelect(columns), columns, getExcludeStaticWhere());
    }

    @Override
    public Class<Join> getAnnotationClass() {
        return Join.class;
    }

    @Override
    public String parseTableName() {
        return annotation.joinTable();
    }

}
