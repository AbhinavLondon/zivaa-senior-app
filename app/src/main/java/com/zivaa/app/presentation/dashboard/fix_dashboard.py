with open('DashboardScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_content = lines[:2064]
new_content.extend([
    '                }\n',
    '            }\n',
    '        }\n',
    '    }\n',
    '}\n',
    '\n',
    '@Composable\n',
    'fun LongevityCard(onNavigateToLongevity: () -> Unit) {\n',
    '    Surface(\n',
    '        color = ZivaaTheme.colors.bgElev,\n',
    '        shape = RoundedCornerShape(16.dp),\n',
    '        modifier = Modifier\n',
    '            .fillMaxWidth()\n',
    '            .clickable { onNavigateToLongevity() }\n',
    '            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), ambientColor = ZivaaTheme.colors.ink.copy(alpha=0.05f))\n',
    '    ) {\n',
    '        Row(\n',
    '            modifier = Modifier.padding(16.dp),\n',
    '            verticalAlignment = Alignment.CenterVertically,\n',
    '            horizontalArrangement = Arrangement.SpaceBetween\n',
    '        ) {\n',
    '            Column(modifier = Modifier.weight(1f)) {\n',
    '                Text(\n',
    '                    text = "My Longevity Plan",\n',
    '                    style = ZivaaTheme.typography.bodyLarge,\n',
    '                    color = ZivaaTheme.colors.ink\n',
    '                )\n',
    '                Spacer(modifier = Modifier.height(4.dp))\n',
    '                Text(\n',
    '                    text = "Track your daily habits and strategy",\n',
    '                    style = ZivaaTheme.typography.meta,\n',
    '                    color = ZivaaTheme.colors.inkMute\n',
    '                )\n',
    '            }\n',
    '            Box(\n',
    '                modifier = Modifier\n',
    '                    .size(40.dp)\n',
    '                    .clip(RoundedCornerShape(999.dp))\n',
    '                    .background(ZivaaTheme.colors.sage.copy(alpha = 0.15f)),\n',
    '                contentAlignment = Alignment.Center\n',
    '            ) {\n',
    '                Icon(\n',
    '                    imageVector = Icons.Default.ArrowForward,\n',
    '                    contentDescription = "Go to Longevity Plan",\n',
    '                    tint = ZivaaTheme.colors.sage,\n',
    '                    modifier = Modifier.size(20.dp)\n',
    '                )\n',
    '            }\n',
    '        }\n',
    '    }\n',
    '}\n'
])

with open('DashboardScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(new_content)

print('File successfully restored!')
