import zipfile
jar_path = 'C:/Users/faten/.m2/repository/com/openai/openai-java-core/0.8.1/openai-java-core-0.8.1.jar'
with zipfile.ZipFile(jar_path, 'r') as z:
    names = [n for n in z.namelist() if 'ChatCompletionUser' in n]
    for n in sorted(names):
        print(n)
    print("---")
    for cls_name in names:
        data = z.read(cls_name)
        text = data.decode('latin-1')
        if 'ofString' in text or 'ofText' in text:
            print(f"IN {cls_name}: ofString={'ofString' in text}, ofText={'ofText' in text}")
