package com.jjp.jsu.compare;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LCS(Longest Common Subsequence) 알고리즘 기반 텍스트 Diff 서비스.
 * 줄(line) 단위로 비교하며, 연속된 REMOVED+ADDED 쌍은 유사도에 따라 MODIFIED 로 병합합니다.
 * 각 변경 항목은 PriorityService 의 규칙 엔진(R01~R10)으로 평가됩니다.
 */
@Service
public class DiffService {

    private static final double SIMILARITY_THRESHOLD = 0.4;

    private final PriorityService priorityService;

    public DiffService(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    /**
     * 이전 텍스트와 새 텍스트를 비교하여 변경 항목 목록을 반환합니다.
     * 결과는 점수(높은 순) → 변경 유형 순으로 정렬됩니다.
     */
    public List<ChangeItem> diff(String oldText, String newText) {
        String[] oldLines = splitAndTrim(oldText);
        String[] newLines = splitAndTrim(newText);

        List<int[]> rawDiff = computeRawDiff(oldLines, newLines);
        List<ChangeItem> changes = new ArrayList<>();

        int i = 0;
        while (i < rawDiff.size()) {
            int[] entry = rawDiff.get(i);
            int diffType = entry[0]; // 0=unchanged, 1=removed, 2=added

            if (diffType == 0) {
                i++;
                continue;
            }

            // 연속된 REMOVED + ADDED → 유사하면 MODIFIED 로 병합
            if (diffType == 1 && i + 1 < rawDiff.size() && rawDiff.get(i + 1)[0] == 2) {
                String oldContent = oldLines[entry[1]];
                String newContent = newLines[rawDiff.get(i + 1)[2]];

                if (isSimilar(oldContent, newContent)) {
                    ScoringResult result = priorityService.evaluate("MODIFIED", oldContent, newContent);
                    changes.add(new ChangeItem("MODIFIED", result.score(), result.priority(),
                            oldContent, newContent, result.reasons()));
                    i += 2;
                    continue;
                }
            }

            if (diffType == 1) {
                String content = oldLines[entry[1]];
                ScoringResult result = priorityService.evaluate("REMOVED", content, "");
                changes.add(new ChangeItem("REMOVED", result.score(), result.priority(),
                        content, "", result.reasons()));
            } else {
                String content = newLines[entry[2]];
                ScoringResult result = priorityService.evaluate("ADDED", "", content);
                changes.add(new ChangeItem("ADDED", result.score(), result.priority(),
                        "", content, result.reasons()));
            }
            i++;
        }

        // 점수 내림차순 정렬
        changes.sort(Comparator.comparingInt(ChangeItem::score).reversed());
        return changes;
    }

    /**
     * LCS DP 를 이용해 raw diff 항목 목록을 생성합니다.
     * int[] 구성: [type, oldIndex, newIndex]
     *   type 0 = unchanged, 1 = removed, 2 = added
     */
    private List<int[]> computeRawDiff(String[] oldLines, String[] newLines) {
        int m = oldLines.length;
        int n = newLines.length;

        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (oldLines[i - 1].trim().equals(newLines[j - 1].trim())) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        List<int[]> result = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && oldLines[i - 1].trim().equals(newLines[j - 1].trim())) {
                result.add(0, new int[]{0, i - 1, j - 1});
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                result.add(0, new int[]{2, -1, j - 1});
                j--;
            } else {
                result.add(0, new int[]{1, i - 1, -1});
                i--;
            }
        }
        return result;
    }

    /** 공통 문자 수 비율로 두 줄의 유사도를 판단합니다. */
    private boolean isSimilar(String a, String b) {
        if (a.isBlank() || b.isBlank()) return false;
        int common = countCommonChars(a.toLowerCase(), b.toLowerCase());
        int longer = Math.max(a.length(), b.length());
        return (double) common / longer > SIMILARITY_THRESHOLD;
    }

    private int countCommonChars(String a, String b) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : a.toCharArray()) freq.merge(c, 1, Integer::sum);
        int count = 0;
        for (char c : b.toCharArray()) {
            if (freq.getOrDefault(c, 0) > 0) {
                count++;
                freq.merge(c, -1, Integer::sum);
            }
        }
        return count;
    }

    private String[] splitAndTrim(String text) {
        if (text == null || text.isBlank()) return new String[0];
        String[] lines = text.split("\n", -1);
        int end = lines.length;
        while (end > 0 && lines[end - 1].isBlank()) end--;
        return Arrays.copyOf(lines, end);
    }
}
