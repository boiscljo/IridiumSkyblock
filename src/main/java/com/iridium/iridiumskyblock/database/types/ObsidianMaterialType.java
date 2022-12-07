package com.iridium.iridiumskyblock.database.types;


import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import java.sql.SQLException;
import java.util.Optional;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

public class ObsidianMaterialType extends StringType {

    private static final ObsidianMaterialType instance = new ObsidianMaterialType();

    public static ObsidianMaterialType getSingleton() {
        return instance;
    }

    protected ObsidianMaterialType() {
        super(SqlType.STRING, new Class<?>[] { ObsidianMaterial.class });
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String value = (String) super.sqlArgToJava(fieldType, sqlArg, columnPos);
        ObsidianMaterial material = ObsidianMaterial.valueOf(value);
        return material;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object object) throws SQLException {
        ObsidianMaterial material = (ObsidianMaterial) object;
        return super.javaToSqlArg(fieldType, material.toString());
    }
    
}
