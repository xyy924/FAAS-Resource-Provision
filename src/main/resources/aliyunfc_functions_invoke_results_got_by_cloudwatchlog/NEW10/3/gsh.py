import pandas as pd
import os
import glob


def remove_column_4_from_xls(file_path):
    """删除xls文件的第四列"""
    try:
        # 根据文件扩展名选择引擎
        if file_path.endswith('.xls'):
            engine = 'xlrd'  # 用于旧版xls文件
        else:
            engine = 'openpyxl'  # 用于xlsx文件

        # 读取Excel文件
        df = pd.read_excel(file_path, engine=engine)

        # 检查是否有第四列
        if len(df.columns) >= 5:
            # 删除第四列（索引为3）
            column_name = df.columns[4]
            df.drop(column_name, axis=1, inplace=True)

            # 保存文件，根据扩展名选择引擎
            if file_path.endswith('.xls'):
                # 对于xls文件，保存为xlsx格式以避免兼容性问题
                new_path = file_path.replace('.xls', '.xlsx')
                df.to_excel(new_path, index=False, engine='openpyxl')
                print(f"已处理: {file_path} -> 转换为 {new_path} (删除列: {column_name})")

                # 删除原xls文件
                os.remove(file_path)
                print(f"已删除原文件: {file_path}")
            else:
                df.to_excel(file_path, index=False, engine='openpyxl')
                print(f"已处理: {file_path} (删除列: {column_name})")
        else:
            print(f"跳过（列数不足）: {file_path}")

    except Exception as e:
        print(f"处理失败 {file_path}: {str(e)}")


def batch_remove_xls_column(directory="."):
    """批量处理目录中的所有xls文件"""
    # 查找所有xls和xlsx文件
    xls_files = glob.glob(os.path.join(directory, "*.xls"))
    xlsx_files = glob.glob(os.path.join(directory, "*.xlsx"))
    all_files = xls_files + xlsx_files

    if not all_files:
        print("未找到xls或xlsx文件")
        return

    print(f"找到 {len(all_files)} 个Excel文件")

    for file_path in all_files:
        remove_column_4_from_xls(file_path)

    print("批量处理完成！")


# 使用示例
if __name__ == "__main__":
    batch_remove_xls_column(".")